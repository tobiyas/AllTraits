/*******************************************************************************
 * Copyright 2014 Tob
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package trait;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import de.tobiyas.racesandclasses.datacontainer.player.RaCPlayer;
import de.tobiyas.racesandclasses.eventprocessing.eventresolvage.EventWrapper;
import de.tobiyas.racesandclasses.eventprocessing.eventresolvage.PlayerAction;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.TraitResults;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitConfigurationField;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitConfigurationNeeded;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitEventsUsed;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitInfos;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.markerinterfaces.Trait;
import de.tobiyas.racesandclasses.traitcontainer.traits.passive.AbstractPassiveTrait;
import de.tobiyas.racesandclasses.util.bukkit.versioning.compatibility.CompatibilityModifier;
import de.tobiyas.racesandclasses.util.traitutil.TraitConfiguration;
import de.tobiyas.racesandclasses.util.traitutil.TraitConfigurationFailedException;

public class PickaxeDamageIncreaseTrait extends AbstractPassiveTrait{
	
	
	public PickaxeDamageIncreaseTrait(){
	}
	
	@TraitEventsUsed(registerdClasses = {EntityDamageByEntityEvent.class})
	@Override
	public void generalInit(){
	}

	@Override
	public String getName() {
		return "PickaxeDamageIncreaseTrait";
	}


	@Override
	protected String getPrettyConfigIntern(){
		return operation + " " +  value;
	}

	@TraitConfigurationNeeded( fields = {
			@TraitConfigurationField(fieldName = "operation", classToExpect = String.class), 
			@TraitConfigurationField(fieldName = "value", classToExpect = Double.class)
		})
	@Override
	public void setConfiguration(TraitConfiguration configMap) throws TraitConfigurationFailedException {
		super.setConfiguration(configMap);
		operation = (String) configMap.get("operation");
		value = (Double) configMap.get("value");
	}
	
	@Override
	public TraitResults trigger(EventWrapper eventWrapper) {   Event event = eventWrapper.getEvent();
		EntityDamageByEntityEvent Eevent = (EntityDamageByEntityEvent) event;
		double newValue = getNewValue(eventWrapper.getPlayer(), Eevent.getDamage());
		
		CompatibilityModifier.EntityDamage.safeSetDamage(newValue, Eevent);
		return TraitResults.True();
	}
	
	private boolean checkItemIsPickaxe(ItemStack stack){
		Material item = stack.getType();
		if(item == Material.WOOD_PICKAXE)
			return true;
		
		if(item == Material.STONE_PICKAXE)
			return true;
		
		if(item == Material.GOLD_PICKAXE)
			return true;
		
		if(item == Material.IRON_PICKAXE)
			return true;
		
		if(item == Material.DIAMOND_PICKAXE)
			return true;
			
		return false;
	}

	@Override
	public boolean isBetterThan(Trait trait) {
		if(!(trait instanceof PickaxeDamageIncreaseTrait)) return false;
		PickaxeDamageIncreaseTrait otherTrait = (PickaxeDamageIncreaseTrait) trait;
		
		return value >= otherTrait.value;
	}

	@TraitInfos(category="passive", traitName="PickaxeDamageIncreaseTrait", visible=true)
	@Override
	public void importTrait() {
	}
	
	public static List<String> getHelpForTrait(){
		List<String> helpList = new LinkedList<String>();
		helpList.add(ChatColor.YELLOW + "The trait increases the Damage of your Pickaxe.");
		return helpList;
	}

	@Override
	public boolean canBeTriggered(EventWrapper wrapper) {
		if(wrapper.getPlayerAction() != PlayerAction.DO_DAMAGE) return false;
		
		RaCPlayer causer = wrapper.getPlayer(); 		
		if(!checkItemIsPickaxe(causer.getPlayer().getItemInHand())) return false;
		return true;
	}	
	
}
