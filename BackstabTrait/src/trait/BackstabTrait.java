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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import de.tobiyas.racesandclasses.APIs.LanguageAPI;
import de.tobiyas.racesandclasses.eventprocessing.eventresolvage.EventWrapper;
import de.tobiyas.racesandclasses.eventprocessing.eventresolvage.PlayerAction;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.AbstractBasicTrait;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.TraitResults;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitConfigurationField;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitConfigurationNeeded;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitEventsUsed;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitInfos;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.markerinterfaces.Trait;
import de.tobiyas.racesandclasses.translation.languages.Keys;
import de.tobiyas.racesandclasses.util.entitysearch.SearchEntity;
import de.tobiyas.racesandclasses.util.traitutil.TraitConfigurationFailedException;

public class BackstabTrait extends AbstractBasicTrait implements Listener {

	/**
	 * the amount of blocks searched.
	 */
	private int blocks = 0;
	
	//The Potion effects done.
	private int strength = 0;
	private int duration = 0;
	
	
	@TraitEventsUsed(registerdClasses = {PlayerInteractEntityEvent.class, PlayerInteractEvent.class})
	@Override
	public void generalInit() {
	}
	
	
	@Override
	public String getName(){
		return "BackstabTrait";
	}

	
	@Override
	protected String getPrettyConfigIntern(){
		return "distance: " + blocks + " blocks.";
	}

	@TraitConfigurationNeeded(fields = {
			@TraitConfigurationField(fieldName = "blocks", classToExpect = Integer.class, optional = false),
			@TraitConfigurationField(fieldName = "strength", classToExpect = Integer.class, optional = true),
			@TraitConfigurationField(fieldName = "duration", classToExpect = Integer.class, optional = true),			
		})
	@Override
	public void setConfiguration(Map<String, Object> configMap) throws TraitConfigurationFailedException {
		super.setConfiguration(configMap);
		
		blocks = (Integer) configMap.get("blocks");
		
		if(configMap.containsKey("strength")){
			strength = (Integer) configMap.get("strength");
		}
		
		if(configMap.containsKey("duration")){
			duration = (Integer) configMap.get("duration");
		}
	}

	
	@Override
	public TraitResults trigger(EventWrapper eventWrapper) {   Event event = eventWrapper.getEvent();
		Player player = null;
		LivingEntity target = null;
		
		if(event instanceof PlayerInteractEntityEvent){
			PlayerInteractEntityEvent Eevent = (PlayerInteractEntityEvent) event;
			target = (LivingEntity) Eevent.getRightClicked();
			player = Eevent.getPlayer();
		}
		
		if(event instanceof PlayerInteractEvent && target == null){
			PlayerInteractEvent Eevent = (PlayerInteractEvent) event;
			player = Eevent.getPlayer();
			if(Eevent.getAction() == Action.RIGHT_CLICK_AIR){
				target = SearchEntity.inLineOfSight(blocks, player);
			}
		}
		
		
		if(player == null || target == null) return TraitResults.False();
		
		
		Location targetLocation = target.getLocation().add(0, 0.3, 0);
		Location playerLocation = player.getLocation();
		
		//check worlds
		if(targetLocation.getWorld() != playerLocation.getWorld()){
			return TraitResults.False();
		}
		
		//check distance
		if(targetLocation.distance(playerLocation) > blocks){
			return TraitResults.False();
		}
		
		LanguageAPI.sendTranslatedMessage(player, Keys.trait_backstab_success, "name", target.getType().name());
		targetLocation.subtract(targetLocation.getDirection());
		
		Location searchLocation = targetLocation.clone();
		if(searchLocation.add(0, 1, 0).getBlock().getType() != Material.AIR){
			targetLocation = target.getLocation();
		}
		
		searchLocation = targetLocation.clone();
		if(searchLocation.add(0, 2, 0).getBlock().getType() != Material.AIR){
			targetLocation = target.getLocation();
		}
		
		player.teleport(targetLocation);
		
		if(strength > 0 && duration > 0){
			player.addPotionEffect(new PotionEffect(
					PotionEffectType.INCREASE_DAMAGE, 
					duration * 20, 
					strength)
			);
		}
		
		return TraitResults.True();
	}
	
	
	public static List<String> getHelpForTrait(){
		List<String> helpList = new LinkedList<String>();
		helpList.add(ChatColor.YELLOW + "The trait lets you Poison your Weapon.");
		helpList.add(ChatColor.YELLOW + "Poison your weapon with a ROSE in the Workbench.");
		
		return helpList;
	}
	
	@Override
	public boolean isBetterThan(Trait trait) {
		if(!(trait instanceof BackstabTrait)) return false;
		BackstabTrait otherTrait = (BackstabTrait) trait;
		
		return blocks >= otherTrait.blocks;
	}


	@TraitInfos(category="activate", traitName="BackstabTrait", visible=true)
	@Override
	public void importTrait() {
	}

	@Override
	public boolean canBeTriggered(EventWrapper wrapper) {
		PlayerAction action = wrapper.getPlayerAction();
		if(!(action == PlayerAction.INTERACT_AIR 
				|| action == PlayerAction.INTERACT_ENTITY)) return false;
		
		if(action == PlayerAction.INTERACT_AIR){
			return true;
		}
		
		if(action == PlayerAction.INTERACT_ENTITY){
			return true;
		}
		
		return false;
	}

	@Override
	public boolean triggerButHasUplink(EventWrapper wrapper) {
		//Not needed
		return false;
	}
	
	@Override
	public boolean notifyTriggeredUplinkTime(EventWrapper wrapper) {
		return true;
	}
	
}
