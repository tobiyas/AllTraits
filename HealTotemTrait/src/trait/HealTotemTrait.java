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
import org.bukkit.Effect;
import org.bukkit.entity.Player;

import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitConfigurationField;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitConfigurationNeeded;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitInfos;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.markerinterfaces.Trait;
import de.tobiyas.racesandclasses.traitcontainer.traits.pattern.AbstractTotemTrait;
import de.tobiyas.racesandclasses.util.bukkit.versioning.compatibility.CompatibilityModifier;
import de.tobiyas.racesandclasses.util.traitutil.TraitConfigurationFailedException;

public class HealTotemTrait extends AbstractTotemTrait {

	/**
	 * The Value to heal.
	 */
	private double value = 1;
	

	@Override
	public String getName() {
		return "HealTotemTrait";
	}


	@TraitConfigurationNeeded( fields = {
			@TraitConfigurationField(fieldName = "value", classToExpect = Double.class, optional = false)
		})
	@Override
	public void setConfiguration(Map<String, Object> configMap) throws TraitConfigurationFailedException {
		super.setConfiguration(configMap);
		
		if(configMap.containsKey("value")){
			value = (Double) configMap.get("value");
		}
	}

	public static List<String> getHelpForTrait(){
		List<String> helpList = new LinkedList<String>();
		helpList.add(ChatColor.YELLOW + "This Trait places a totem on the ground that heals everyone in range.");
		return helpList;
	}
	

	@TraitInfos(category = "totem", traitName = "HealTotemTrait", visible = true)
	@Override
	public void importTrait() {
		super.importTrait();
	}


	@Override
	public boolean isBetterThan(Trait trait) {
		return false;
	}


	@Override
	protected void tickOn(TotemInfos infos, Player player) {
		CompatibilityModifier.BukkitPlayer.safeHeal(value, player);
		player.getLocation().getWorld().playEffect(player.getLocation().add(0, 1, 0), Effect.ENDER_SIGNAL, 0);
	}


	@Override
	protected String getPrettyConfigIntern() {
		return "Heals " + value + " every " + tickEvery / 20;
	}


}
