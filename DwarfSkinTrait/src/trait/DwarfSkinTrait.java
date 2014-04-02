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
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import de.tobiyas.racesandclasses.APIs.CooldownApi;
import de.tobiyas.racesandclasses.APIs.LanguageAPI;
import de.tobiyas.racesandclasses.APIs.MessageScheduleApi;
import de.tobiyas.racesandclasses.configuration.traits.TraitConfig;
import de.tobiyas.racesandclasses.eventprocessing.eventresolvage.EventWrapper;
import de.tobiyas.racesandclasses.eventprocessing.eventresolvage.PlayerAction;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.TraitResults;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitConfigurationField;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitConfigurationNeeded;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitEventsUsed;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitInfos;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.markerinterfaces.Trait;
import de.tobiyas.racesandclasses.traitcontainer.traits.passive.AbstractPassiveTrait;
import de.tobiyas.racesandclasses.translation.languages.Keys;
import de.tobiyas.racesandclasses.util.bukkit.versioning.compatibility.CompatibilityModifier;
import de.tobiyas.racesandclasses.util.traitutil.TraitConfigurationFailedException;

public class DwarfSkinTrait extends AbstractPassiveTrait {
	
	private static int duration = 10;
	private static double activationLimit = 30;
	

	public DwarfSkinTrait(){
	}
	
	
	@TraitEventsUsed(registerdClasses = {EntityDamageByEntityEvent.class})
	@Override
	public void generalInit() {		
		TraitConfig config = plugin.getConfigManager().getTraitConfigManager().getConfigOfTrait(getName());
		if(config != null){
			duration = (Integer) config.getValue("trait.duration", 10);
			activationLimit = config.getDouble("trait.activationLimit", 30);
		}
	}

	@Override
	public String getName() {
		return "DwarfSkinTrait";
	}


	@Override
	protected String getPrettyConfigIntern(){
		return "damage Reduce: " + operation + " " + value;
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
		if(!(event instanceof EntityDamageEvent)) return TraitResults.False();
		EntityDamageEvent Eevent = (EntityDamageEvent) event;
		Entity entity = Eevent.getEntity();
		
		if(entity.getType() != EntityType.PLAYER) return TraitResults.False();
		Player player = (Player) entity;
		
		double maxHealth = plugin.getPlayerManager().getMaxHealthOfPlayer(player.getUniqueId());
		double currentHealth =  plugin.getPlayerManager().getHealthOfPlayer(player.getUniqueId());
		double healthPercent = 100 * currentHealth / maxHealth;
		if(healthPercent > activationLimit) return TraitResults.False();
		
		active(player);
		if(checkIfActive(player)){
			double oldValue = CompatibilityModifier.EntityDamage.safeGetDamage(Eevent);
			double newValue = getNewValue(oldValue);
			
			CompatibilityModifier.EntityDamage.safeSetDamage(newValue, Eevent);
			return TraitResults.True();
		}
		
		return TraitResults.False();
	}
	
	private void active(Player player){
		LanguageAPI.sendTranslatedMessage(player, Keys.trait_toggled, "name", getDisplayName());
		MessageScheduleApi.scheduleTranslateMessageToPlayer(player.getName(), duration, Keys.trait_faded, "name", getDisplayName());
	}
	
	
	private boolean checkIfActive(Player player){
		int remainingTime = CooldownApi.getCooldownOfPlayer(player.getName(), "trait." + getName());
		return (remainingTime - (cooldownTime - duration)) > 0;
	}
	
	
	@Override
	public boolean isBetterThan(Trait trait) {
		if(!(trait instanceof DwarfSkinTrait)) return false;
		DwarfSkinTrait otherTrait = (DwarfSkinTrait) trait;
		
		return value >= otherTrait.value;
	}

	@TraitInfos(category="passive", traitName="DwarfSkinTrait", visible=true)
	@Override
	public void importTrait() {
	}


	public static List<String> getHelpForTrait(){
		List<String> helpList = new LinkedList<String>();
		helpList.add(ChatColor.YELLOW + "The trait decreases your taken damage when you drop below " + activationLimit + "% health.");
		helpList.add(ChatColor.YELLOW + "This only lasts " + duration + " seconds and only occurs every " + "X" + " seconds");
		return helpList;
	}


	@Override
	public boolean canBeTriggered(EventWrapper wrapper) {
		if(wrapper.getPlayerAction() != PlayerAction.TAKE_DAMAGE) return false;
		Player player = wrapper.getPlayer();
		
		double maxHealth = plugin.getPlayerManager().getMaxHealthOfPlayer(player.getUniqueId());
		double currentHealth =  plugin.getPlayerManager().getHealthOfPlayer(player.getUniqueId());
		double healthPercent = 100 * currentHealth / maxHealth;
		if(healthPercent > activationLimit) return false;
		
		return true;
	}


	@Override
	public boolean notifyTriggeredUplinkTime(EventWrapper wrapper) {
		return false;
	}
	
}
