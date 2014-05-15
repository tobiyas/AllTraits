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
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import de.tobiyas.racesandclasses.APIs.LanguageAPI;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.TraitResults;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitConfigurationField;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitConfigurationNeeded;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitEventsUsed;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitInfos;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.markerinterfaces.Trait;
import de.tobiyas.racesandclasses.traitcontainer.traits.magic.AbstractMagicSpellTrait;
import de.tobiyas.racesandclasses.translation.languages.Keys;
import de.tobiyas.racesandclasses.util.entitysearch.SearchEntity;
import de.tobiyas.racesandclasses.util.traitutil.TraitConfigurationFailedException;

public class PushAwayTrait extends AbstractMagicSpellTrait  {

	/**
	 * The Blocks the enemy is pushed away.
	 */
	private int blocks = 0;
	
	@TraitEventsUsed(registerdClasses = {PlayerInteractEvent.class})
	@Override
	public void generalInit() {
	}

	@Override
	public String getName() {
		return "PushAwayTrait";
	}

	
	@Override
	protected String getPrettyConfigIntern(){
		return "pushes " + blocks + " blocks away.";
	}

	
	@TraitInfos(category="magic", traitName="PushAwayTrait", visible=true)
	@Override
	public void importTrait() {
	}

	
	@Override
	public boolean isBetterThan(Trait trait) {
		if(!(trait instanceof PushAwayTrait)) return false;
		
		PushAwayTrait otherTrait = (PushAwayTrait) trait;
		return cost > otherTrait.cost;
	}

	
	public static List<String> getHelpForTrait(){
		List<String> helpList = new LinkedList<String>();
		helpList.add(ChatColor.YELLOW + "This trait pushes Entities away.");
		return helpList;
	}



	@TraitConfigurationNeeded(fields = {
			@TraitConfigurationField( fieldName = "blocks", classToExpect = Integer.class)
		})
	@Override
	public void setConfiguration(Map<String, Object> configMap) throws TraitConfigurationFailedException {
		super.setConfiguration(configMap);
		
		this.blocks = (Integer) configMap.get("blocks");
	}
	
	@Override
	protected void magicSpellTriggered(Player player, TraitResults result) {
		LivingEntity pushbackEntity = SearchEntity.inLineOfSight(30, player);		
		if(pushbackEntity != null){
			String targetName = pushbackEntity.getType() == EntityType.PLAYER 
					? ((Player)pushbackEntity).getName() 
					: pushbackEntity.getType().name();
			
			LanguageAPI.sendTranslatedMessage(player, Keys.trait_pushaway_success, "target", targetName);
			
			Vector playerVector = player.getLocation().getDirection();
			playerVector.setY(0);
			
			playerVector.multiply(this.blocks);
			playerVector.setY(0.2);
			
			pushbackEntity.setVelocity(playerVector);
			result.setTriggered(true);
			return;
		}
		
		result.setTriggered(false);
		return;
	}
	
}