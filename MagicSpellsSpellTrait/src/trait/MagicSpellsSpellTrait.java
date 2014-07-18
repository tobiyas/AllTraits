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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.Spell.SpellCastResult;
import com.nisovin.magicspells.Spell.SpellCastState;

import de.tobiyas.racesandclasses.datacontainer.player.RaCPlayer;
import de.tobiyas.racesandclasses.eventprocessing.eventresolvage.EventWrapper;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.TraitResults;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.bypasses.NeedsOtherPlugins;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitConfigurationField;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitConfigurationNeeded;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitInfos;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.markerinterfaces.Trait;
import de.tobiyas.racesandclasses.traitcontainer.traits.magic.AbstractMagicSpellTrait;
import de.tobiyas.racesandclasses.util.traitutil.TraitConfiguration;
import de.tobiyas.racesandclasses.util.traitutil.TraitConfigurationFailedException;

@NeedsOtherPlugins(neededPlugins = {"MagicSpells"})
public class MagicSpellsSpellTrait extends AbstractMagicSpellTrait implements Listener {

	/**
	 * The permissions to give
	 */
	private String magicSpellName;

	
	@Override
	public String getName() {
		return "MagicSpellsSpellTrait";
	}


	@TraitConfigurationNeeded( fields = {
			@TraitConfigurationField(fieldName = "name", classToExpect = String.class, optional = false)
	})
	@Override
	public void setConfiguration(TraitConfiguration configMap) throws TraitConfigurationFailedException {
		super.setConfiguration(configMap);
		
		if(configMap.containsKey("name")){
			this.magicSpellName = configMap.getAsString("name");
		}
	}

	public static List<String> getHelpForTrait(){
		List<String> helpList = new LinkedList<String>();
		helpList.add(ChatColor.YELLOW + "This trait Sets a permission.");
		return helpList;
	}
	

	@TraitInfos(category = "passive", traitName = "MagicSpellsSpellTrait", visible = true)
	@Override
	public void importTrait() {
	}

	
	@Override
	protected String getPrettyConfigIntern() {
		return "Cast magic spell: " + magicSpellName;
	}
	
	
	@Override
	public boolean isStackable(){
		return true;
	}


	@Override
	public boolean isBetterThan(Trait trait) {
		return false;
	}

	
	@Override
	public boolean canBeTriggered(EventWrapper wrapper) {
		//if MagicSpells is not present, return false.
		if(Bukkit.getPluginManager().getPlugin("MagicSpells") == null) return false;
		
		return super.canBeTriggered(wrapper);
	}

	@Override
	protected void magicSpellTriggered(RaCPlayer player, TraitResults result) {
		if(Bukkit.getPluginManager().getPlugin("MagicSpells") == null) return;
		
		MagicSpells magicSpells = MagicSpells.getInstance();
		if(magicSpells == null) return;
		
		Spell spell = MagicSpells.getSpellByInGameName(magicSpellName);
		if(spell == null) spell = MagicSpells.getSpellByInternalName(magicSpellName);
		
		if(spell == null){
			result.copyFrom(TraitResults.False());
			return;
		}
		
		SpellCastResult spellResult = spell.cast(player.getPlayer());
		if(spellResult.state == SpellCastState.NORMAL) {
			result.copyFrom(TraitResults.True());			
		}else{
			result.copyFrom(TraitResults.False());
		}
	}
}
