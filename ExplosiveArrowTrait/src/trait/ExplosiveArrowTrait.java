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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

import de.tobiyas.racesandclasses.eventprocessing.TraitEventManager;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitConfigurationField;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitConfigurationNeeded;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitEventsUsed;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitInfos;
import de.tobiyas.racesandclasses.traitcontainer.traits.arrows.AbstractArrow;
import de.tobiyas.racesandclasses.util.bukkit.versioning.compatibility.CompatibilityModifier;
import de.tobiyas.racesandclasses.util.traitutil.TraitConfigurationFailedException;

public class ExplosiveArrowTrait extends AbstractArrow{
	
	private HashMap<Arrow, Player> arrowMap;
	private boolean destroyBlocks = false;

	public ExplosiveArrowTrait(){
	}
	
	@TraitEventsUsed(registerdClasses = {})
	@Override
	public void generalInit(){
		arrowMap = new HashMap<Arrow, Player>();
	}
	
	@Override
	public String getName() {
		return "ExplosiveArrowTrait";
	}

	@Override
	protected String getPrettyConfigIntern(){
		return "radius: " + duration + " damage: " + totalDamage;
	}

	@TraitConfigurationNeeded( fields = {
			@TraitConfigurationField(fieldName = "radius", classToExpect = Integer.class), 
			@TraitConfigurationField(fieldName = "damage", classToExpect = Double.class),
			@TraitConfigurationField(fieldName = "explode", classToExpect = Boolean.class, optional = true)
		})
	@Override
	public void setConfiguration(Map<String, Object> configMap) throws TraitConfigurationFailedException {
		super.setConfiguration(configMap);
		duration = (Integer) configMap.get("radius");
		totalDamage = (Double) configMap.get("damage");
		
		if(configMap.containsKey("explode")){
			destroyBlocks = (Boolean) configMap.get("explode");
		}else{
			destroyBlocks = false;
		}
	}
	

	@Override
	protected boolean onShoot(EntityShootBowEvent event) {
		if(!(event.getProjectile() instanceof Arrow)) return false;
		if(!(event.getEntity() instanceof Player)) return false;
		Arrow arrow = (Arrow) event.getProjectile();
		Player player = (Player) event.getEntity();
		
		arrowMap.put(arrow, player);
		return false;
	}

	@Override
	protected boolean onHitEntity(EntityDamageByEntityEvent event) {
		if(!(event.getEntity() instanceof Arrow)) return false;
		if(!arrowMap.containsKey(event.getEntity())) return false;
		Arrow arrow = (Arrow) event.getEntity();
		
		Location loc = arrow.getLocation();
		if(destroyBlocks){
			loc.getWorld().createExplosion(loc, duration);
			arrowMap.remove(arrow);
			return true;
		}else
			loc.getWorld().createExplosion(loc, 0);
		
		HashSet<LivingEntity> damageTo = getEntitiesNear(loc, duration);
		
		for(LivingEntity entity : damageTo){
			Event newEvent = CompatibilityModifier.EntityDamage
					.safeCreateEvent(entity, DamageCause.BLOCK_EXPLOSION, totalDamage);
			
			TraitEventManager.fireEvent(newEvent);
		}
		
		arrowMap.remove(arrow);
		return false;
	}
	
	private HashSet<LivingEntity> getEntitiesNear(Location loc, int radius){
		Location locToCheck = loc.getBlock().getLocation();
		HashSet<LivingEntity> entitySet = new HashSet<LivingEntity>();
		
		for(Entity entity : loc.getWorld().getEntities()){
			if(!(entity instanceof LivingEntity)) continue;
			if(entity.getLocation().getBlock().getLocation().distance(locToCheck) < radius)
				entitySet.add((LivingEntity)entity);
		}
		
		return entitySet;
	}

	@Override
	protected boolean onHitLocation(ProjectileHitEvent event) {
		if(!(event.getEntity() instanceof Arrow)) return false;
		if(!arrowMap.containsKey(event.getEntity())) return false;
		Arrow arrow = (Arrow) event.getEntity();
		
		Location loc = arrow.getLocation();
		if(destroyBlocks){
			loc.getWorld().createExplosion(loc, duration);
			arrowMap.remove(arrow);
			return true;
		}else{
			loc.getWorld().createExplosion(loc, 0);
		}
		
		HashSet<LivingEntity> damageTo = getEntitiesNear(loc, duration);
		arrowMap.remove(arrow);
		
		for(LivingEntity entity : damageTo){
			Event newEvent = CompatibilityModifier.EntityDamage
					.safeCreateEvent(entity, DamageCause.BLOCK_EXPLOSION, totalDamage);
			
			TraitEventManager.fireEvent(newEvent);
		}
		
		return false;
	}

	@Override
	protected String getArrowName() {
		return "Explosive Arrow";
	}

	@TraitInfos(category="arrow", traitName="ExplosiveArrowTrait", visible=true)
	@Override
	public void importTrait() {
	}

	public static List<String> getHelpForTrait(){
		List<String> helpList = new LinkedList<String>();
		helpList.add(ChatColor.YELLOW + "When you fire an Arrow and have selected the ExplosiveArrowTrait, it explodes on hit.");
		return helpList;
	}
}
