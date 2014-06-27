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
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.HorseJumpEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;

import de.tobiyas.racesandclasses.eventprocessing.eventresolvage.EventWrapper;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.AbstractBasicTrait;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.TraitResults;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.bypasses.NeedMC1_6;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitConfigurationField;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitConfigurationNeeded;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitEventsUsed;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitInfos;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.markerinterfaces.Trait;
import de.tobiyas.racesandclasses.util.traitutil.TraitConfiguration;
import de.tobiyas.racesandclasses.util.traitutil.TraitConfigurationFailedException;

@NeedMC1_6
public class HorseRestrictionTrait extends AbstractBasicTrait {

	private final String MOUNT_PATH = "mount";
	private final String LEASH_PATH = "leash";
	private final String CHEST_PATH = "chest";
	private final String JUMPING_PATH = "jumping";
	private final String TAME_PATH = "tame";
	
	
	/**
	 * The Permission to mount a horse.
	 */
	private boolean mount;
	
	/**
	 * The Permission to leash a horse.
	 */
	private boolean leash;
	
	/**
	 * The Permission to give a horse a chest.
	 */
	private boolean chest;
	
	/**
	 * The Permission to tame a horse.
	 */
	private boolean tame;
	
	
	/**
	 * The Permission to jump with a horse
	 */
	private boolean jumping;
	
	
	@TraitEventsUsed(registerdClasses = {EntityTameEvent.class, HorseJumpEvent.class, 
			PlayerLeashEntityEvent.class, PlayerUnleashEntityEvent.class, 
			PlayerInteractEntityEvent.class})
	@Override
	public void generalInit() {
		this.chest = true;
		this.jumping = true;
		this.leash = true;
		this.mount = true;
		this.tame = true;
	}

	
	@Override
	public String getName() {
		return "HorseRestrictionTrait";
	}

	
	@Override
	protected String getPrettyConfigIntern(){
		String forbidden = "forbidden: ";
		
		if(!mount) forbidden += "mounting ";
		if(!chest) forbidden += "chesting ";
		if(!tame) forbidden += "taming ";
		if(!leash) forbidden += "leashing ";
		if(!jumping) forbidden += "jumping";
		
		if(forbidden.equals("forbidden: ")) forbidden += "nothing. :)";
		
		return forbidden;
	}

	
	@TraitInfos(category="passive", traitName="HorseRestrictionTrait", visible=true)
	@Override
	public void importTrait() {
	}

	
	@Override
	public boolean isBetterThan(Trait trait) {
		return false;
	}

	
	@Override
	public TraitResults trigger(EventWrapper eventWrapper) {   Event event = eventWrapper.getEvent();
		if(event instanceof EntityTameEvent && !tame){
			((Cancellable)event).setCancelled(true);
			return TraitResults.True();
		}
		
		if(event instanceof PlayerInteractEntityEvent && (!mount || !chest)){
			PlayerInteractEntityEvent pieEvent = (PlayerInteractEntityEvent) event;
			
			Player player = pieEvent.getPlayer();
			if(!chest && pieEvent.getRightClicked().getType() == EntityType.HORSE &&
					player.getItemInHand().getType() == Material.CHEST){
				
				pieEvent.setCancelled(true);
				return TraitResults.True();
			}
			
			
			if(pieEvent.getRightClicked().getType() == EntityType.HORSE && !mount){
				pieEvent.setCancelled(true);
				return TraitResults.True();
			}
			
			return TraitResults.False();
		}
		
		
		if(event instanceof HorseJumpEvent && !jumping){
			HorseJumpEvent hjEvent = (HorseJumpEvent) event;
			hjEvent.setCancelled(true);
			return TraitResults.True();
		}
		
		if(event instanceof PlayerLeashEntityEvent && !leash){
			PlayerLeashEntityEvent pleEvent = (PlayerLeashEntityEvent) event;
			pleEvent.setCancelled(true);
			return TraitResults.True();
		}
		
		if(event instanceof PlayerUnleashEntityEvent && !leash){
			((Cancellable)event).setCancelled(true);
			return TraitResults.True();
		}
		
		
		//TODO prevent chesting.
		
		return TraitResults.False();
	}

	
	@TraitConfigurationNeeded(fields = {
			@TraitConfigurationField(fieldName = LEASH_PATH, classToExpect = Boolean.class, optional = true),
			@TraitConfigurationField(fieldName = CHEST_PATH, classToExpect = Boolean.class, optional = true),
			@TraitConfigurationField(fieldName = JUMPING_PATH, classToExpect = Boolean.class, optional = true),
			@TraitConfigurationField(fieldName = MOUNT_PATH, classToExpect = Boolean.class, optional = true),
			@TraitConfigurationField(fieldName = TAME_PATH, classToExpect = Boolean.class, optional = true)
	})
	@Override
	public void setConfiguration(TraitConfiguration configMap) throws TraitConfigurationFailedException {
		super.setConfiguration(configMap);
		if(configMap.containsKey(LEASH_PATH)){
			this.leash = (Boolean) configMap.get(LEASH_PATH);
		}
		
		if(configMap.containsKey(CHEST_PATH)){
			this.chest = (Boolean) configMap.get(CHEST_PATH);
		}
		
		if(configMap.containsKey(JUMPING_PATH)){
			this.jumping = (Boolean) configMap.get(JUMPING_PATH);
		}
		
		if(configMap.containsKey(MOUNT_PATH)){
			this.mount = (Boolean) configMap.get(MOUNT_PATH);
		}
		
		if(configMap.containsKey(TAME_PATH)){
			this.tame = (Boolean) configMap.get(TAME_PATH);
		}
		
	}


	public static List<String> getHelpForTrait(){
		List<String> helpList = new LinkedList<String>();
		helpList.add(ChatColor.YELLOW + "The trait limits the usage of Horses.");
		helpList.add(ChatColor.YELLOW + "You can limit mounting, taming, leashing, putting chests and jumping.");
		return helpList;
	}


	@Override
	public boolean canBeTriggered(EventWrapper wrapper) {
		Event event = wrapper.getEvent();
		if(event instanceof EntityTameEvent && !tame){
			return true;
		}
		
		if(event instanceof PlayerInteractEntityEvent && (!mount || !chest)){
			PlayerInteractEntityEvent pieEvent = (PlayerInteractEntityEvent) event;
			
			Player player = pieEvent.getPlayer();
			if(!chest && pieEvent.getRightClicked().getType() == EntityType.HORSE &&
					player.getItemInHand().getType() == Material.CHEST){
				
				return true;
			}
			
			
			if(pieEvent.getRightClicked().getType() == EntityType.HORSE && !mount){
				return true;
			}
			
			return false;
		}
		
		
		if(event instanceof HorseJumpEvent && !jumping){
			HorseJumpEvent hjEvent = (HorseJumpEvent) event;
			Entity passenger = hjEvent.getEntity().getPassenger();
			if(passenger == null || !(passenger instanceof Player) ) return false;
			
			return true;
		}
		
		if(event instanceof PlayerLeashEntityEvent && !leash){
			PlayerLeashEntityEvent pleEvent = (PlayerLeashEntityEvent) event;
			if(pleEvent.getEntity().getType() != EntityType.HORSE) return false;  
			
			return true;
		}
		
		if(event instanceof PlayerUnleashEntityEvent && !leash){
			PlayerUnleashEntityEvent pleEvent = (PlayerUnleashEntityEvent) event;
			if(pleEvent.getEntity().getType() != EntityType.HORSE) return false;  
			
			return true;
		}
		
		
		//TODO prevent chesting.
		
		return false;
	}

}
