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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import de.tobiyas.racesandclasses.eventprocessing.eventresolvage.EventWrapper;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.AbstractBasicTrait;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.TraitResults;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitConfigurationField;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitConfigurationNeeded;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitEventsUsed;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitInfos;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.markerinterfaces.Trait;
import de.tobiyas.racesandclasses.util.traitutil.TraitConfiguration;
import de.tobiyas.racesandclasses.util.traitutil.TraitConfigurationFailedException;

public class GrapplingHookTrait extends AbstractBasicTrait{
	
	private Material materialToUse = Material.ARROW;
	
	private Map<Projectile, String> launchMap = new HashMap<Projectile, String>();
	
	@TraitEventsUsed(registerdClasses = {PlayerInteractEvent.class, ProjectileHitEvent.class})
	@Override
	public void generalInit(){
	}

	@Override
	public String getName() {
		return "GrapplingHookTrait";
	}
	
	
	@Override
	protected String getPrettyConfigIntern() {
		return "fire a Grappling hook.";
	}

	@TraitConfigurationNeeded(fields = {
				@TraitConfigurationField(fieldName = "material", classToExpect = Material.class)
		})
	@Override
	public void setConfiguration(TraitConfiguration configMap) throws TraitConfigurationFailedException {
		super.setConfiguration(configMap);
		
		materialToUse = (Material) configMap.get("material");
	}
	

	@Override
	public TraitResults trigger(EventWrapper eventWrapper) {   Event event = eventWrapper.getEvent();
		if(event instanceof PlayerInteractEvent){
			PlayerInteractEvent Eevent = (PlayerInteractEvent) event;
			Player player = Eevent.getPlayer();
			
			Arrow projectile = player.launchProjectile(Arrow.class);
			launchMap.put(projectile, player.getName());
			return TraitResults.True();
		}
		
		if(event instanceof ProjectileHitEvent){
			ProjectileHitEvent hitEvent = ((ProjectileHitEvent)event);
			Projectile projectile = hitEvent.getEntity();
			
			Player player = Bukkit.getPlayer(launchMap.get(projectile));
			
			launchMap.remove(projectile);
			projectile.remove();

			pullPlayerTo(player, projectile.getLocation());
		}
		
		return TraitResults.True();
	}
	
	
	private void pullPlayerTo(final Player player, final Location location) {
		BukkitRunnable runnable = new BukkitRunnable() {
			
			@Override
			public void run() {
				Location playerLocation = player.getLocation();
				if(playerLocation.distance(location) < 0.4 || launchMap.values().contains(player.getName())){
					cancel();
				}
				
				Vector vec = location.getDirection().subtract(playerLocation.getDirection());
				vec.normalize().multiply(0.4);
				
				Location teleportLocation = playerLocation.add(vec);
				player.teleport(teleportLocation);
			}
		};
		
		Bukkit.getScheduler().scheduleSyncRepeatingTask((JavaPlugin)plugin, runnable, 5, 5);
	
	}

	public static List<String> getHelpForTrait(){
		List<String> helpList = new LinkedList<String>();
		helpList.add(ChatColor.YELLOW + "You fire a grappling hook which lifts you.");
		return helpList;
	}
	
	@Override
	public boolean isBetterThan(Trait trait) {
		return false;
	}
	
	@TraitInfos(category="activate", traitName="GrapplingHookTrait", visible=true)
	@Override
	public void importTrait() {
	}

	@Override
	public boolean canBeTriggered(EventWrapper wrapper) {
		Event event = wrapper.getEvent(); //TODO fix sometime...
		if(event instanceof PlayerInteractEvent){
			PlayerInteractEvent Eevent = (PlayerInteractEvent) event;
			Player player = Eevent.getPlayer();
			if(player.getItemInHand().getType() == materialToUse
					&& Eevent.getAction() == Action.RIGHT_CLICK_AIR) return true;
		}
		
		if(event instanceof ProjectileHitEvent){
			Projectile projectile = ((ProjectileHitEvent) event).getEntity();
			
			if(launchMap.containsKey(projectile)){
				return true;
			}
		}
		
		return false;
	}
	
}
