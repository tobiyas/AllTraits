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
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import de.tobiyas.racesandclasses.datacontainer.player.RaCPlayer;
import de.tobiyas.racesandclasses.datacontainer.traitholdercontainer.AbstractTraitHolder;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.RemoveSuperConfigField;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitConfigurationField;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitConfigurationNeeded;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitInfos;
import de.tobiyas.racesandclasses.traitcontainer.traits.pattern.TickEverySecondsTrait;
import de.tobiyas.racesandclasses.util.traitutil.TraitConfiguration;
import de.tobiyas.racesandclasses.util.traitutil.TraitConfigurationFailedException;
import de.tobiyas.racesandclasses.vollotile.Vollotile;

public class PermanentPotionTrait extends TickEverySecondsTrait {

	
	/**
	 * The amplifier of the Potion
	 */
	private int amplifier = 0;
	
	/**
	 * The type of the Potion
	 */
	private PotionEffectType type;
	
	/**
	 * if the Particles should be removed.
	 */
	private boolean removeParticles = false;
	

	@Override
	public String getName() {
		return "PermanentPotionTrait";
	}


	@TraitConfigurationNeeded( fields = {
			@TraitConfigurationField(fieldName = "amplifier", classToExpect = Integer.class, optional = false),
			@TraitConfigurationField(fieldName = "type", classToExpect = Integer.class, optional = false),
			@TraitConfigurationField(fieldName = "removeParticles", classToExpect = Boolean.class, optional = true)
		}, removedFields = {
			@RemoveSuperConfigField(name = "seconds")
	})
	@Override
	public void setConfiguration(TraitConfiguration configMap) throws TraitConfigurationFailedException {
		//Put the overriding stuff in it.
		configMap.put("seconds", (Integer)5);
		super.setConfiguration(configMap);
		
		
		amplifier = (Integer) configMap.get("amplifier");
		type = PotionEffectType.getById((Integer) configMap.get("type"));

		if(configMap.containsKey("removeParticles")){
			removeParticles = (Boolean) configMap.get("removeParticles");
		}
		
		if(type == null){
			throw new TraitConfigurationFailedException("unknown Potion effect.");
		}
	}

	public static List<String> getHelpForTrait(){
		List<String> helpList = new LinkedList<String>();
		helpList.add(ChatColor.YELLOW + "This trait aplies a potion effect permanently to the player.");
		return helpList;
	}
	

	@TraitInfos(category = "passive", traitName = "PermanentPotionTrait", visible = true)
	@Override
	public void importTrait() {
	}

	
	
	int particleTaskID = -1;

	@Override
	protected boolean tickDoneForPlayer(RaCPlayer player) {
		boolean replace = true;
		for(PotionEffect effect : player.getPlayer().getActivePotionEffects()){
			if(effect.getType() == type && effect.getDuration() > 20 * 15){
				replace = false;
				break;
			}
		}
		
		if(replace){
			PotionEffect newPotionEffect = new PotionEffect(type, 20 * 15, amplifier);				
			player.getPlayer().addPotionEffect(newPotionEffect, true);
			
			if(removeParticles){
				Vollotile.get().removeParticleEffect(player.getPlayer());
			}
		}
		
		if(removeParticles && particleTaskID < 0){
			particleTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask((JavaPlugin)plugin, new Runnable() {
				
				@Override
				public void run() {
					for(AbstractTraitHolder holder : PermanentPotionTrait.this.getTraitHolders()){
						for(RaCPlayer player : holder.getHolderManager().getAllPlayersOfHolder(holder)){
							if(player != null && player.isOnline()){
								Vollotile.get().removeParticleEffect(player.getPlayer());
							}
						}
					}
					
				}
			}, 5, 5);
		}
		
		return true;
	}

	@Override
	protected String getPrettyConfigurationPre() {
		return "potion: " + type.getName() + " amplifier: " + amplifier;
	}
	
	@Override
	public boolean isStackable(){
		return true;
	}
}
