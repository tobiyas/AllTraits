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
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import de.tobiyas.racesandclasses.eventprocessing.eventresolvage.EventWrapper;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitConfigurationField;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitConfigurationNeeded;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitInfos;
import de.tobiyas.racesandclasses.traitcontainer.traits.pattern.TickEverySecondsTrait;
import de.tobiyas.racesandclasses.util.bukkit.versioning.compatibility.CompatibilityModifier;
import de.tobiyas.racesandclasses.util.traitutil.TraitConfigurationFailedException;

public class SunDamageTrait extends TickEverySecondsTrait {

	
	/**
	 * The Damage done if set correct
	 */
	private double damage = 0;
	
	/**
	 * If the player needs to be standing in the Sun
	 */
	private boolean standing = true;
	

	@Override
	public String getName() {
		return "SunDamageTrait";
	}


	@TraitConfigurationNeeded( fields = {
			@TraitConfigurationField(fieldName = "damage", classToExpect = Double.class),
			@TraitConfigurationField(fieldName = "standing", classToExpect = Boolean.class, optional = true)
		})
	@Override
	public void setConfiguration(Map<String, Object> configMap) throws TraitConfigurationFailedException {
		configMap.put("onlyOnDay", true);
		super.setConfiguration(configMap);
		
		damage = (Double) configMap.get("damage");
		
		if(configMap.containsKey("standing")){
			standing = (Boolean) configMap.get("standing");
		}
		
		onlyOnDay = true;
	}

	public static List<String> getHelpForTrait(){
		List<String> helpList = new LinkedList<String>();
		helpList.add(ChatColor.YELLOW + "This trait does damage when standing in the sun.");
		return helpList;
	}
	

	@TraitInfos(category = "passive", traitName = "SunDamageTrait", visible = true)
	@Override
	public void importTrait() {
	}

	@Override
	public boolean canBeTriggered(EventWrapper wrapper) {
		if(!standing) return true;
		
		//check if standing in sun.
		Player player = wrapper.getPlayer();
		int playerYLocation = player.getLocation().getBlockY();
		int highestLocation = player.getWorld().getHighestBlockYAt(player.getLocation());
		if(highestLocation < playerYLocation) return false;

		return true;
	}

	@Override
	protected boolean tickDoneForPlayer(Player player) {
		EntityDamageEvent damageEvent = CompatibilityModifier.EntityDamage.safeCreateEvent(player, DamageCause.FIRE, damage);
		plugin.fireEventToBukkit(damageEvent);
		
		if(!damageEvent.isCancelled() || damageEvent.getDamage() <= 0){
			return false;
		}
		
		CompatibilityModifier.BukkitPlayer.safeDamage(CompatibilityModifier.EntityDamage.safeGetDamage(damageEvent), player);
		return true;
	}

	@Override
	protected String getPrettyConfigurationPre() {
		return "Damage: " + damage;
	}
}
