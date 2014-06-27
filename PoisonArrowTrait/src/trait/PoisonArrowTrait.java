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
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffectTypeWrapper;

import de.tobiyas.racesandclasses.playermanagement.health.damagetickers.DamageTicker;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitConfigurationField;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitConfigurationNeeded;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitEventsUsed;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitInfos;
import de.tobiyas.racesandclasses.traitcontainer.traits.arrows.AbstractArrow;
import de.tobiyas.racesandclasses.util.friend.EnemyChecker;
import de.tobiyas.racesandclasses.util.traitutil.TraitConfiguration;
import de.tobiyas.racesandclasses.util.traitutil.TraitConfigurationFailedException;

public class PoisonArrowTrait extends AbstractArrow {

	public PoisonArrowTrait(){
	}
	
	@TraitEventsUsed(registerdClasses = {EntityDamageByEntityEvent.class, PlayerInteractEvent.class, EntityShootBowEvent.class})
	@Override
	public void generalInit(){
	}
	
	@Override
	public String getName() {
		return "PoisonArrowTrait";
	}

	@Override
	protected String getPrettyConfigIntern(){
		return "damage: " + this.totalDamage + " Poison-Damage over " + duration + " seconds.";
	}
	
	@TraitConfigurationNeeded(fields = {
			@TraitConfigurationField(fieldName = "duration", classToExpect = Integer.class),
			@TraitConfigurationField(fieldName = "totalDamage", classToExpect = Double.class)
		})
	@Override
	public void setConfiguration(TraitConfiguration configMap) throws TraitConfigurationFailedException {
		super.setConfiguration(configMap);
		
		duration = (Integer) configMap.get("duration");
		totalDamage = (Double) configMap.get("totalDamage");
	}
	
	@Override
	protected boolean onShoot(EntityShootBowEvent event) {
		//Not needed
		return false;
	}

	@Override
	protected boolean onHitEntity(EntityDamageByEntityEvent event) {
		Entity hitTarget = event.getEntity();
		if(!(hitTarget instanceof LivingEntity)) return false;
		
		if(EnemyChecker.areAllies(event.getDamager(), hitTarget)) return false;
		
		double damagePerTick = totalDamage / duration;
		DamageTicker ticker = new DamageTicker((LivingEntity) hitTarget, duration, damagePerTick, DamageCause.POISON, event.getDamager());
		ticker.linkPotionEffect(PotionEffectTypeWrapper.POISON.createEffect(duration, 0));
		return true;
	}

	@Override
	protected boolean onHitLocation(ProjectileHitEvent event) {
		//Not needed
		return false;
	}

	@Override
	protected String getArrowName() {
		return "Poison Arrow";
	}

	@TraitInfos(category="arrow", traitName="PoisonArrowTrait", visible=true)
	@Override
	public void importTrait() {
	}
	
	public static List<String> getHelpForTrait(){
		List<String> helpList = new LinkedList<String>();
		helpList.add(ChatColor.YELLOW + "When you hit an enemy with an arrow and the PoisonArrowTrait is selected,");
		helpList.add(ChatColor.YELLOW + "the player will get poison damage periodicaly till the timer is done.");
		return helpList;
	}
	
}
