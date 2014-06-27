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
import org.bukkit.entity.Fireball;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import de.tobiyas.racesandclasses.APIs.LanguageAPI;
import de.tobiyas.racesandclasses.datacontainer.player.RaCPlayer;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.TraitResults;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitEventsUsed;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitInfos;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.markerinterfaces.Trait;
import de.tobiyas.racesandclasses.traitcontainer.traits.magic.AbstractMagicSpellTrait;
import de.tobiyas.racesandclasses.translation.languages.Keys;

public class FireballTrait extends AbstractMagicSpellTrait  {

	@TraitEventsUsed(registerdClasses = {PlayerInteractEvent.class})
	@Override
	public void generalInit() {
	}

	@Override
	public String getName() {
		return "FireballTrait";
	}

	
	@Override
	protected String getPrettyConfigIntern(){
		return costType.name() + (materialForCasting == null ? " " : (" " + materialForCasting.name() + " ")) + cost;
	}

	
	@TraitInfos(category="activate", traitName="FireballTrait", visible=true)
	@Override
	public void importTrait() {
	}

	
	@Override
	public boolean isBetterThan(Trait trait) {
		if(!(trait instanceof FireballTrait)) return false;
		
		FireballTrait otherTrait = (FireballTrait) trait;
		return cost > otherTrait.cost;
	}

	
	public static List<String> getHelpForTrait(){
		List<String> helpList = new LinkedList<String>();
		helpList.add(ChatColor.YELLOW + "This trait fires a Fireball.");
		return helpList;
	}


	@Override
	protected void magicSpellTriggered(RaCPlayer player, TraitResults result) {
		Vector viewDirection = player.getLocation().getDirection();
		if(viewDirection == null){
			LanguageAPI.sendTranslatedMessage(player, Keys.no_taget_found);
			result.setTriggered(false);
			return;
		}
		
		Fireball fireball = player.getPlayer().launchProjectile(Fireball.class);
		fireball.setVelocity(viewDirection);
		
		LanguageAPI.sendTranslatedMessage(player, Keys.launched_something, "name", "Fireball");
		result.setTriggered(true);
		return;
	}
	
}
