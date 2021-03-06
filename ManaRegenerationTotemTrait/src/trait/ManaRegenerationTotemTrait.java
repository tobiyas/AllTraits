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
import org.bukkit.Effect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import de.tobiyas.racesandclasses.datacontainer.player.RaCPlayer;
import de.tobiyas.racesandclasses.datacontainer.player.RaCPlayerManager;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitConfigurationField;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitConfigurationNeeded;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitInfos;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.markerinterfaces.Trait;
import de.tobiyas.racesandclasses.traitcontainer.traits.pattern.AbstractTotemTrait;
import de.tobiyas.racesandclasses.util.traitutil.TraitConfiguration;
import de.tobiyas.racesandclasses.util.traitutil.TraitConfigurationFailedException;

public class ManaRegenerationTotemTrait extends AbstractTotemTrait {

	/**
	 * The Value to regenerate Mana.
	 */
	private double value = 1;
	

	@Override
	public String getName() {
		return "ManaRegenerationTotemTrait";
	}


	@TraitConfigurationNeeded( fields = {
			@TraitConfigurationField(fieldName = "value", classToExpect = Double.class, optional = false)
		})
	@Override
	public void setConfiguration(TraitConfiguration configMap) throws TraitConfigurationFailedException {
		super.setConfiguration(configMap);
		
		if(configMap.containsKey("value")){
			value = (Double) configMap.get("value");
		}
	}

	public static List<String> getHelpForTrait(){
		List<String> helpList = new LinkedList<String>();
		helpList.add(ChatColor.YELLOW + "This Trait places a totem on the ground that fills Mana for everyone in range.");
		return helpList;
	}
	

	@TraitInfos(category = "totem", traitName = "ManaRegenerationTotemTrait", visible = true)
	@Override
	public void importTrait() {
	}


	@Override
	public boolean isBetterThan(Trait trait) {
		return false;
	}


	@Override
	protected void tickOnPlayer(TotemInfos infos, Player player) {
		RaCPlayer racPlayer = RaCPlayerManager.get().getPlayer(player);
		
		double modValue = modifyToPlayer(infos.getOwner(), value, "value");
		racPlayer.getManaManager().fillMana(modValue);
		player.getLocation().getWorld().playEffect(player.getLocation().add(0, 1, 0), Effect.ENDER_SIGNAL, 0);
	}


	@Override
	protected String getPrettyConfigIntern() {
		return "Regenerates " + value + " Mana every " + tickEvery / 20;
	}


	@Override
	protected void tickOnNonPlayer(TotemInfos infos, LivingEntity entity) {
		//Not needed.
	}

}
