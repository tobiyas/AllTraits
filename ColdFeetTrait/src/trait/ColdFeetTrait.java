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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import de.tobiyas.racesandclasses.APIs.LanguageAPI;
import de.tobiyas.racesandclasses.APIs.MessageScheduleApi;
import de.tobiyas.racesandclasses.eventprocessing.eventresolvage.EventWrapper;
import de.tobiyas.racesandclasses.eventprocessing.eventresolvage.PlayerAction;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.TraitResults;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitConfigurationField;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitConfigurationNeeded;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitEventsUsed;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitInfos;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.markerinterfaces.Trait;
import de.tobiyas.racesandclasses.traitcontainer.traits.magic.AbstractMagicSpellTrait;
import de.tobiyas.racesandclasses.translation.languages.Keys;
import de.tobiyas.racesandclasses.util.traitutil.TraitConfigurationFailedException;

public class ColdFeetTrait extends AbstractMagicSpellTrait {

	/**
	 * The duration of this spell
	 */
	private int duration;
	
	/**
	 * Tells the Trait to turn the ice back to water.
	 */
	private boolean turnBack = true;
	
	
	private final List<String> coldFeetList = new LinkedList<String>();
	
	private final List<ScheduleBackToWater> blocks = new LinkedList<ScheduleBackToWater>();
	
	
	@TraitEventsUsed(bypassClasses = {BlockBreakEvent.class},
			registerdClasses = {PlayerInteractEvent.class, PlayerMoveEvent.class})
	@Override
	public void generalInit() {
	}
	

	@Override
	public String getName(){
		return "ColdFeetTrait";
	}

	
	@Override
	protected String getPrettyConfigIntern(){
		return "for: " + duration + " seconds, mana: " + cost;
	}

	@TraitConfigurationNeeded( fields = {
			@TraitConfigurationField(fieldName = "duration", classToExpect = Integer.class), 
			@TraitConfigurationField(fieldName = "turnBack", classToExpect = Boolean.class, optional = true)
		})
	@Override
	public void setConfiguration(Map<String, Object> configMap) throws TraitConfigurationFailedException {
		super.setConfiguration(configMap);
		
		duration = (Integer) configMap.get("duration");
		if(configMap.containsKey("turnBack")){
			turnBack = (Boolean) configMap.get("turnBack");
		}
	}
	
	
	public static List<String> getHelpForTrait(){
		List<String> helpList = new LinkedList<String>();
		helpList.add(ChatColor.YELLOW + "The trait lets you freeze water below your feet.");
		return helpList;
	}
	
	@Override
	public boolean isBetterThan(Trait trait) {
		if(!(trait instanceof ColdFeetTrait)) return false;
		ColdFeetTrait otherTrait = (ColdFeetTrait) trait;
		
		return (cost / duration) <= (otherTrait.cost / otherTrait.duration);
	}


	@TraitInfos(category="magic", traitName="ColdFeetTrait", visible=true)
	@Override
	public void importTrait() {
	}
	


	@Override
	public boolean canBeTriggered(EventWrapper wrapper) {
		if(wrapper.getEvent() instanceof BlockBreakEvent){
			BlockBreakEvent event = (BlockBreakEvent) wrapper.getEvent();
			for(ScheduleBackToWater thread : blocks){
				if(thread.getBlock() == event.getBlock()){
					event.setCancelled(true);
					return false;
				}
			}
		}
		
		if(wrapper.getPlayerAction() == PlayerAction.PLAYER_MOVED){
			return coldFeetList.contains(wrapper.getPlayer().getName());
		}
		
		return super.canBeTriggered(wrapper);
	}


	@Override
	public TraitResults trigger(EventWrapper eventWrapper) {
		/*
		if(event instanceof PlayerMoveEvent){
			PlayerMoveEvent playerMoveEvent = (PlayerMoveEvent) event;
			Player player = playerMoveEvent.getPlayer();
			
			//freeze water below feet
			Location belowPlayerLocation = player.getLocation().subtract(0,1,0);
			List<Block> blocksToCheck = new LinkedList<Block>();
						
			blocksToCheck.add(belowPlayerLocation.getBlock());
			blocksToCheck.add(belowPlayerLocation.getBlock().getRelative(BlockFace.NORTH));
			blocksToCheck.add(belowPlayerLocation.getBlock().getRelative(BlockFace.EAST));
			blocksToCheck.add(belowPlayerLocation.getBlock().getRelative(BlockFace.SOUTH));
			blocksToCheck.add(belowPlayerLocation.getBlock().getRelative(BlockFace.WEST));
			
			for(Block block : blocksToCheck){
				Material blockMaterial = block.getType();
				if(blockMaterial == Material.WATER || blockMaterial == Material.STATIONARY_WATER){
					if(turnBack){
						new ScheduleBackToWater(block, duration);
					}else{
						block.setType(Material.ICE);
					}
				}
			}
			
			return TraitResults.False();
		}*/ //TODO check if worked
		
		return super.trigger(eventWrapper);
	}

	@Override
	protected TraitResults otherEventTriggered(EventWrapper eventWrapper, TraitResults result){
		if(eventWrapper.getEvent() instanceof PlayerMoveEvent){
			PlayerMoveEvent playerMoveEvent = (PlayerMoveEvent) eventWrapper.getEvent();
			Player player = playerMoveEvent.getPlayer();
			
			//freeze water below feet
			Location belowPlayerLocation = player.getLocation().subtract(0,1,0);
			List<Block> blocksToCheck = new LinkedList<Block>();
						
			blocksToCheck.add(belowPlayerLocation.getBlock());
			blocksToCheck.add(belowPlayerLocation.getBlock().getRelative(BlockFace.NORTH));
			blocksToCheck.add(belowPlayerLocation.getBlock().getRelative(BlockFace.EAST));
			blocksToCheck.add(belowPlayerLocation.getBlock().getRelative(BlockFace.SOUTH));
			blocksToCheck.add(belowPlayerLocation.getBlock().getRelative(BlockFace.WEST));
			
			for(Block block : blocksToCheck){
				Material blockMaterial = block.getType();
				if(blockMaterial == Material.WATER || blockMaterial == Material.STATIONARY_WATER){
					if(turnBack){
						new ScheduleBackToWater(block, duration);
					}else{
						block.setType(Material.ICE);
					}
				}
			}
			
			return TraitResults.False();
		}
		
		if(eventWrapper.getEvent() instanceof BlockBreakEvent){
			
		}
		
		return result;
	}

	@Override
	protected void magicSpellTriggered(Player player, TraitResults result) {
		final String playerName = player.getName();
		
		if(coldFeetList.contains(playerName)){
			LanguageAPI.sendTranslatedMessage(player, Keys.trait_already_active, "name", getDisplayName());
			result.setTriggered(false);
			return;
		}
		
		coldFeetList.add(playerName);
		LanguageAPI.sendTranslatedMessage(player, Keys.trait_toggled, "name", getDisplayName());
		MessageScheduleApi.scheduleTranslateMessageToPlayer(playerName, duration, Keys.trait_faded, "name", getDisplayName());
		
		Bukkit.getScheduler().scheduleSyncDelayedTask((JavaPlugin)plugin, new Runnable() {	
			@Override
			public void run() {
				coldFeetList.remove(playerName);
			}
		}, 20 * duration);
		
		result.setTriggered(true);
	}
	
	@Override
	public void triggerButDoesNotHaveEnoghCostType(EventWrapper wrapper){
		if(wrapper.getPlayerAction() == PlayerAction.PLAYER_MOVED){
			trigger(wrapper);
			return; //containing list is checked in CanBeTriggered already
		}
		
		super.triggerButDoesNotHaveEnoghCostType(wrapper);
		return;
	}
}
