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

import java.security.SecureRandom;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import de.tobiyas.racesandclasses.APIs.LanguageAPI;
import de.tobiyas.racesandclasses.datacontainer.player.RaCPlayer;
import de.tobiyas.racesandclasses.datacontainer.player.RaCPlayerManager;
import de.tobiyas.racesandclasses.eventprocessing.eventresolvage.EventWrapper;
import de.tobiyas.racesandclasses.eventprocessing.eventresolvage.PlayerAction;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.AbstractBasicTrait;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.TraitResults;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitConfigurationField;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitConfigurationNeeded;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitEventsUsed;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitInfos;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.markerinterfaces.Trait;
import de.tobiyas.racesandclasses.translation.languages.Keys;
import de.tobiyas.racesandclasses.util.friend.EnemyChecker;
import de.tobiyas.racesandclasses.util.traitutil.TraitConfiguration;
import de.tobiyas.racesandclasses.util.traitutil.TraitConfigurationFailedException;

public class PoisonedWeaponTrait extends AbstractBasicTrait{
	
	private double seconds = 0;
	private double totalDamage = 0;
	
	private double chance = 0.20;
	private int applications = 0;
	
	private Material poinsonMaterial = Material.RED_ROSE;
	
	private Random rand = new SecureRandom();
	
	private PoisonWeaponListener listener;
	
	@TraitEventsUsed(registerdClasses = {EntityDamageByEntityEvent.class})
	@Override
	public void generalInit(){
		listener = new PoisonWeaponListener(this, applications, poinsonMaterial);
	}
	

	@Override
	public void deInit() {
		super.deInit();
		
		listener.deregister();
		listener = null;
	}


	@Override
	public String getName() {
		return "PoisonedWeaponTrait";
	}
	
	@Override
	protected String getPrettyConfigIntern() {
		return "Poison damage: " + totalDamage + " in " +  seconds + "s.";
	}

	@TraitConfigurationNeeded( fields = {
			@TraitConfigurationField(fieldName = "duration", classToExpect = Double.class, optional = false), 
			@TraitConfigurationField(fieldName = "totaldamage", classToExpect = Double.class, optional = false),
			@TraitConfigurationField(fieldName = "applications", classToExpect = Integer.class, optional = false),
			@TraitConfigurationField(fieldName = "chance", classToExpect = Double.class, optional = true),
			@TraitConfigurationField(fieldName = "poisonMaterial", classToExpect = Material.class, optional = true)
		})
	@Override
	public void setConfiguration(TraitConfiguration configMap) throws TraitConfigurationFailedException {

		super.setConfiguration(configMap);
		
		seconds = (Double) configMap.get("duration");
		totalDamage = (Double) configMap.get("totaldamage");
		
		applications = (Integer) configMap.get("applications");
		
		if(configMap.containsKey("chance")){
			chance = (Double) configMap.get("chance");
		}

		if(configMap.containsKey("poisonMaterial")){
			poinsonMaterial = (Material) configMap.get("poisonMaterial");
		}
	}
	
	
	@Override
	public TraitResults trigger(EventWrapper eventWrapper) {   
		Event event = eventWrapper.getEvent();
		if(!(event instanceof EntityDamageByEntityEvent)) return TraitResults.False();
		
		EntityDamageByEntityEvent Eevent = (EntityDamageByEntityEvent) event;
		if(!(Eevent.getDamager() instanceof Player)) return TraitResults.False();
		
		Player causer = (Player) Eevent.getDamager();
		if(!checkItemIsPoisoned(causer.getItemInHand())) return TraitResults.False();
		
		LivingEntity target = (LivingEntity) Eevent.getEntity();
		double randValue = rand.nextDouble();
		if(chance > randValue){
			if(EnemyChecker.areAllies(causer, target)) return TraitResults.False();
			
			int time = (int) seconds * 20;
			int amplifier = (int) modifyToPlayer(RaCPlayerManager.get().getPlayer(causer), totalDamage);
			
			PotionEffect effect = new PotionEffect(PotionEffectType.POISON, time, amplifier);
			target.addPotionEffect(effect, true);
			
			//since addPotionEffect returnValue is uselesse, we use our own detection.
			String targetName = target instanceof Player ? ((Player) target).getName() : target.getType().name();
			if(!target.hasPotionEffect(PotionEffectType.POISON)){
				LanguageAPI.sendTranslatedMessage(causer, Keys.trait_poison_imun, "target", targetName);
				return TraitResults.False();
			}
			
			reducePoisonOnWeapon(causer.getItemInHand());
			LanguageAPI.sendTranslatedMessage(causer, Keys.trait_poison_success, "target", targetName);
			
			if(target instanceof Player){
				LanguageAPI.sendTranslatedMessage((Player)target, Keys.trait_poison_notify_other,
						"player", causer.getName());
			}
		}
		
		return TraitResults.False();
	}
	
	/**
	 * Reduces the Poison on the Weapon by 1.
	 * 
	 * @param itemInHand to decrease.
	 */
	private void reducePoisonOnWeapon(ItemStack itemInHand) {
		ItemMeta meta = itemInHand.getItemMeta();
		
		List<String> lore = meta.getLore();
		Iterator<String> loreIt = lore.iterator();
		
		while(loreIt.hasNext()){
			String toCheck = loreIt.next();
			if(toCheck.startsWith(PoisonWeaponListener.poisonRecogString)){
				toCheck = toCheck.replace(PoisonWeaponListener.poisonRecogString, "");
				int value = 0;
				try{
					value = Integer.parseInt(toCheck);
				}catch(NumberFormatException exp){}
				value --;
				
				loreIt.remove();
				
				if(value > 0){
					lore.add(PoisonWeaponListener.poisonRecogString + value);
				}
				
				meta.setLore(lore);
				itemInHand.setItemMeta(meta);
				return;
			}
		}
	}


	private boolean checkItemIsPoisoned(ItemStack stack){
		if(stack == null) return false;
		if(!stack.hasItemMeta()) return false;
		ItemMeta meta = stack.getItemMeta();
		if(!meta.hasLore()) return false;
		
		for(String poisonString : meta.getLore()){
			if(poisonString.startsWith(PoisonWeaponListener.poisonRecogString)){
				return true;
			}
		}
		
		return false;
	}
	
	
	public static List<String> getHelpForTrait(){
		List<String> helpList = new LinkedList<String>();
		helpList.add(ChatColor.YELLOW + "When attacking with a Poisoned weapon, it will poison him.");
		helpList.add(ChatColor.YELLOW + "Poison can be added by putting your weapon + a RedRose to a Craftingbench.");
		return helpList;
	}
	
	
	@Override
	public boolean isBetterThan(Trait trait) {
		if(!(trait instanceof PoisonedWeaponTrait)) return false;
		PoisonedWeaponTrait otherTrait = (PoisonedWeaponTrait) trait;
		
		return (totalDamage) >= otherTrait.totalDamage;
	}
	
	@TraitInfos(category="activate", traitName="PoisonedWeaponTrait", visible=true)
	@Override
	public void importTrait() {
	}

	@Override
	public boolean canBeTriggered(EventWrapper wrapper) {
		if(wrapper.getPlayerAction() != PlayerAction.DO_DAMAGE) return false;
		
		RaCPlayer player = wrapper.getPlayer();
 		
		if(!checkItemIsPoisoned(player.getPlayer().getItemInHand())) return false;
		return true;
	}
	
}
