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
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.plugin.java.JavaPlugin;

import de.tobiyas.racesandclasses.eventprocessing.eventresolvage.EventWrapper;
import de.tobiyas.racesandclasses.eventprocessing.eventresolvage.EventWrapperFactory;
import de.tobiyas.racesandclasses.eventprocessing.events.entitydamage.EntityHealEvent;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.AbstractBasicTrait;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.TraitResults;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitConfigurationField;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitConfigurationNeeded;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitEventsUsed;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitInfos;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.markerinterfaces.Trait;
import de.tobiyas.racesandclasses.util.bukkit.versioning.compatibility.CompatibilityModifier;
import de.tobiyas.racesandclasses.util.traitutil.TraitConfigurationFailedException;

public class SpecificRegenerationTrait extends AbstractBasicTrait {

	
	private int schedulerTaskId = -1;
	
	/**
	 * The Seconds when this is fired.
	 */
	private int seconds = 1;
	
	/**
	 * The Damage done if set correct
	 */
	private double heal = 0;
	
	
	@TraitEventsUsed(registerdClasses = {  })
	@Override
	public void generalInit() {
		schedulerTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask((JavaPlugin)plugin, new Runnable() {
			
			@Override
			public void run() {
				for(UUID playerUUID : holder.getHolderManager().getAllPlayersOfHolder(holder)){
					Player player = Bukkit.getPlayer(playerUUID);
					if(player == null || !player.isOnline()) continue;
					
					EventWrapper wrapper = EventWrapperFactory.buildOnlyWithplayer(player.getPlayer());
					if(player != null  && wrapper != null && player.isOnline()
							&& !checkRestrictions(wrapper) 
							&& canBeTriggered(wrapper)){
						
						EntityHealEvent regainHealthEvent = 
								CompatibilityModifier.EntityHeal.safeGenerate(player.getPlayer(), heal, RegainReason.REGEN);
						
						Bukkit.getPluginManager().callEvent(regainHealthEvent);
						if(!regainHealthEvent.isCancelled()){
							double newHealValue = CompatibilityModifier.EntityRegainHealth.safeGetAmount(regainHealthEvent);
							CompatibilityModifier.BukkitPlayer.safeHeal(newHealValue, player.getPlayer());
						}
						
						plugin.getStatistics().traitTriggered(SpecificRegenerationTrait.this);
						
					}
				}
				
				
			}
		}, seconds * 20, seconds * 20);
	}
	
	@Override
	public void deInit(){
		Bukkit.getScheduler().cancelTask(schedulerTaskId);
	}

	@Override
	public String getName() {
		return "SpecificRegenerationTrait";
	}

	@Override
	protected String getPrettyConfigIntern(){
		String reason = "Nothing";
		if(onlyInLava){
			reason = "in Lava";
		}

		if(onlyInWater){
			reason = "in Water";
		}

		if(onlyOnLand){
			reason = "on Land";
		}

		if(onlyOnDay && !onlyInNight){
			reason = "in NightShine";
		}
		
		if(onlyInNight && !onlyOnDay){
			reason = "on DayLight";
		}
		
		return "Damage: " + heal + " every: " + seconds + " sec for " + reason;
	}

	@TraitConfigurationNeeded( fields = {
			@TraitConfigurationField(fieldName = "seconds", classToExpect = Integer.class), 
			@TraitConfigurationField(fieldName = "health", classToExpect = Double.class)
		})
	@Override
	public void setConfiguration(Map<String, Object> configMap) throws TraitConfigurationFailedException {
		super.setConfiguration(configMap);
		
		seconds = (Integer) configMap.get("seconds");
		heal = (Double) configMap.get("health");
	}

	@Override
	public TraitResults trigger(EventWrapper eventWrapper) {   Event event = eventWrapper.getEvent();
		//Not needed
		return TraitResults.False();
	}

	public static List<String> getHelpForTrait(){
		List<String> helpList = new LinkedList<String>();
		helpList.add(ChatColor.YELLOW + "This trait does damage when the Preconditions are correct.");
		return helpList;
	}

	@Override
	public boolean isBetterThan(Trait trait) {
		if (!(trait instanceof SpecificRegenerationTrait))
			return false;
		SpecificRegenerationTrait otherTrait = (SpecificRegenerationTrait) trait;

		return heal >= otherTrait.heal;
	}
	
	@Override
	public boolean isStackable(){
		return true;
	}

	@TraitInfos(category = "passive", traitName = "SpecificRegenerationTrait", visible = true)
	@Override
	public void importTrait() {
	}

	@Override
	public boolean canBeTriggered(EventWrapper wrapper) {
		Player player = wrapper.getPlayer();
		
		int lightFromSky = player.getLocation().getBlock().getLightFromSky();
		if(onlyOnDay){ //TODO fixme
			if(lightFromSky > 2){
				return false;
			}
		}

		return true;
	}
}
