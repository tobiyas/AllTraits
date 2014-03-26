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
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import de.tobiyas.racesandclasses.datacontainer.traitholdercontainer.TraitHolderCombinder;
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
import de.tobiyas.racesandclasses.util.traitutil.TraitConfigurationFailedException;

public class SwordDamageIncreaseTrait extends AbstractPassiveTrait {
	
	
	@TraitEventsUsed(registerdClasses = {EntityDamageByEntityEvent.class})
	@Override
	public void generalInit(){
	}

	@Override
	public String getName() {
		return "SwordDamageIncreaseTrait";
	}
	
	@Override
	protected String getPrettyConfigIntern() {
		return "Sword damage: " + operation + " " +  value;
	}

	@TraitConfigurationNeeded( fields = {
			@TraitConfigurationField(fieldName = "operation", classToExpect = String.class), 
			@TraitConfigurationField(fieldName = "value", classToExpect = Double.class)
		})
	@Override
	public void setConfiguration(Map<String, Object> configMap) throws TraitConfigurationFailedException {
		super.setConfiguration(configMap);
		
		operation = (String) configMap.get("operation");
		value = (Double) configMap.get("value");
	}
	
	
	@Override
	public TraitResults trigger(EventWrapper eventWrapper) {   Event event = eventWrapper.getEvent();
		if(!(event instanceof EntityDamageByEntityEvent)) return TraitResults.False();
		
		EntityDamageByEntityEvent Eevent = (EntityDamageByEntityEvent) event;
		if(!(Eevent.getDamager() instanceof Player)) return TraitResults.False();
		Player causer = (Player) Eevent.getDamager();
 		
		if(TraitHolderCombinder.checkContainer(causer.getName(), this)){
			if(!checkItemIsSword(causer.getItemInHand())) return TraitResults.False();
			
			double oldValue = CompatibilityModifier.EntityDamage.safeGetDamage(Eevent);
			double newValue = getNewValue(oldValue);
			
			CompatibilityModifier.EntityDamage.safeSetDamage(newValue, Eevent);
			return TraitResults.True();
		}
		return TraitResults.False();
	}
	
	private boolean checkItemIsSword(ItemStack stack){
		Material item = stack.getType();
		if(item == Material.WOOD_SWORD)
			return true;
		
		if(item == Material.STONE_SWORD)
			return true;
		
		if(item == Material.GOLD_SWORD)
			return true;
		
		if(item == Material.IRON_SWORD)
			return true;
		
		if(item == Material.DIAMOND_SWORD)
			return true;
			
		return false;
	}
	
	
	public static List<String> getHelpForTrait(){
		List<String> helpList = new LinkedList<String>();
		helpList.add(ChatColor.YELLOW + "Your Damage will be increased by a value or times an value.");
		return helpList;
	}
	
	
	@Override
	public boolean isBetterThan(Trait trait) {
		if(!(trait instanceof SwordDamageIncreaseTrait)) return false;
		SwordDamageIncreaseTrait otherTrait = (SwordDamageIncreaseTrait) trait;
		
		return value >= otherTrait.value;
	}
	
	@TraitInfos(category="passive", traitName="SwordDamageIncreaseTrait", visible=true)
	@Override
	public void importTrait() {
	}

	@Override
	public boolean canBeTriggered(EventWrapper wrapper) {
		if(wrapper.getPlayerAction() != PlayerAction.DO_DAMAGE) return false;
		
		Player causer = wrapper.getPlayer(); 		
		if(!checkItemIsSword(causer.getItemInHand())) return false;
		return true;
	}

}
