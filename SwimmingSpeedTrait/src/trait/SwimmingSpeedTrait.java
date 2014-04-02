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
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.plugin.java.JavaPlugin;

import de.tobiyas.racesandclasses.datacontainer.traitholdercontainer.TraitHolderCombinder;
import de.tobiyas.racesandclasses.eventprocessing.eventresolvage.EventWrapper;
import de.tobiyas.racesandclasses.eventprocessing.events.holderevent.HolderSelectedEvent;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.AbstractBasicTrait;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.TraitResults;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitConfigurationField;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitConfigurationNeeded;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitEventsUsed;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitInfos;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.markerinterfaces.Trait;
import de.tobiyas.racesandclasses.util.traitutil.TraitConfigurationFailedException;

public class SwimmingSpeedTrait extends AbstractBasicTrait {

	private double value;
	
	
	@TraitEventsUsed(registerdClasses = {PlayerLoginEvent.class, HolderSelectedEvent.class})
	@Override
	public void generalInit() {
	}

	@Override
	public String getName() {
		return "SwimmingSpeedTrait";
	}

	@Override
	protected String getPrettyConfigIntern(){
		return "speed: " + value;
	}


	
	@TraitInfos(category="passive", traitName="MovementSpeedTrait", visible=true)
	@Override
	public void importTrait() {
	}

	@Override
	public boolean isBetterThan(Trait trait) {
		if(trait instanceof SwimmingSpeedTrait){
			return value > ((SwimmingSpeedTrait)trait).value;
		}
		return false;
	}

	@Override
	public TraitResults trigger(EventWrapper eventWrapper) {   Event event = eventWrapper.getEvent();
		if(event instanceof PlayerLoginEvent){
			PlayerLoginEvent loginEvent = (PlayerLoginEvent) event;
			final Player player = loginEvent.getPlayer();
			
			
			if(loginEvent.getResult() == Result.ALLOWED){
				if(TraitHolderCombinder.checkContainer(player.getUniqueId(), this)){					
					return new TraitResults( setPlayerSpeed(player));
				}
			}
		}
		

		if(event instanceof HolderSelectedEvent){
			HolderSelectedEvent HolderSelectedEvent = (HolderSelectedEvent) event;
			final Player player = HolderSelectedEvent.getPlayer();			
			return new TraitResults(setPlayerSpeed(player));
		}
		
		return TraitResults.False();
	}
	
	
	/**
	 * Sets the Speed of the player modified to the current Trait.
	 * 
	 * @param player to set
	 * 
	 * @return true if worked, false otherwise
	 */
	private boolean setPlayerSpeed(final Player player){
		final float convertedValue = (float) value;
		if(value < 0 || value > 1) return false;
		
		//Schedule this to the next tics since it will be overwritten...
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((JavaPlugin)plugin, new Runnable() {
			@Override
			public void run() {
				player.setWalkSpeed(convertedValue);
			}
		}, 2);

		return true;
	}

	
	@TraitConfigurationNeeded(fields = {
			@TraitConfigurationField(fieldName = "value", classToExpect = Double.class)
		})
	@Override
	public void setConfiguration(Map<String, Object> configMap) throws TraitConfigurationFailedException {
		super.setConfiguration(configMap);
		
		this.value = (Double) configMap.get("value");
	}

	
	public static List<String> getHelpForTrait(){
		List<String> helpList = new LinkedList<String>();
		helpList.add(ChatColor.YELLOW + "The trait changes (increases or decreases) the movement speed of a Player.");
		return helpList;
	}

	@Override
	public boolean canBeTriggered(EventWrapper wrapper) {
		Event event = wrapper.getEvent();
		if(event instanceof PlayerLoginEvent){
			PlayerLoginEvent loginEvent = (PlayerLoginEvent) event;
			
			if(loginEvent.getResult() == Result.ALLOWED){
				return true;
			}
		}

		if(event instanceof HolderSelectedEvent){
			HolderSelectedEvent HolderSelectedEvent = (HolderSelectedEvent) event;
			if(HolderSelectedEvent.getHolderToSelect() != holder) return false;
			return true;
		}
		
		return false;
	}
}
