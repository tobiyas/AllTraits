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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import de.tobiyas.racesandclasses.RacesAndClasses;

public class PoisonWeaponListener implements Listener{

	public static Material PoisonItem = Material.RED_ROSE;
	
	private final PoisonedWeaponTrait trait;
	
	private final List<Recipe> recipes = new LinkedList<Recipe>();
	
	private int applications = 1;
	
	public static Set<Material> weapons = new HashSet<Material>(Arrays.asList(
			Material.WOOD_SWORD,
			Material.STONE_SWORD,
			Material.GOLD_SWORD,
			Material.IRON_SWORD,
			Material.DIAMOND_SWORD,
			
			Material.WOOD_AXE,
			Material.STONE_AXE,
			Material.GOLD_AXE,
			Material.IRON_AXE,
			Material.DIAMOND_AXE,

			Material.WOOD_HOE,
			Material.STONE_HOE,
			Material.GOLD_HOE,
			Material.IRON_HOE,
			Material.DIAMOND_HOE,
			
			Material.WOOD_SPADE,
			Material.STONE_SPADE,
			Material.GOLD_SPADE,
			Material.IRON_SPADE,
			Material.DIAMOND_SPADE,
			
			Material.WOOD_PICKAXE,
			Material.STONE_PICKAXE,
			Material.GOLD_PICKAXE,
			Material.IRON_PICKAXE,
			Material.DIAMOND_PICKAXE
			));
	
	
	
	public PoisonWeaponListener(PoisonedWeaponTrait trait, int applications, Material poisonMaterial) {
		this.trait = trait;
		this.applications = applications;
		PoisonItem = poisonMaterial;
	
		initRecipes();
		Bukkit.getPluginManager().registerEvents(this, (JavaPlugin)RacesAndClasses.getPlugin());
	}
	
	
	/**
	 * Registers all Recipes.
	 */
	@SuppressWarnings("deprecation") //we need the deprecated method.
	private void initRecipes() {
		for(Material mat : weapons){
			ItemStack result = new ItemStack(mat);
			
			ItemMeta meta = result.getItemMeta();
			List<String> lore = new LinkedList<String>();
			lore.add(poisonRecogString + applications);
			
			meta.setLore(lore);
			result.setItemMeta(meta);
			
			ShapelessRecipe recipe = new ShapelessRecipe(result);

			//Setting durability to max is a workaround for accepting all Durabilities. WTF, MOJANG?!?!?
			recipe.addIngredient(1, mat, Short.MAX_VALUE);
			recipe.addIngredient(PoisonWeaponListener.PoisonItem);
			
			boolean worked = Bukkit.addRecipe(recipe);
			if(worked){
				recipes.add(recipe);
			}
		}
	}

	/**
	 * Removes the registered recipes.
	 */
	public void deregister(){
		Iterator<Recipe> recipeIt = Bukkit.recipeIterator();
		while(recipeIt.hasNext()){
			Recipe toInvestigate = recipeIt.next();
			for(Recipe recipe : recipes){
				if(recipe.getResult().isSimilar(toInvestigate.getResult())){
					recipeIt.remove();
					break;
				}
			}
		}
		
		HandlerList.unregisterAll(this);
	}
	
	public static final String poisonRecogString = ChatColor.GREEN + "Poison: ";

	
	@EventHandler
	public void OnPoisonOnWeapon(CraftItemEvent event){
		Recipe recipe = event.getRecipe();
		ItemStack toCheck = recipe.getResult();
		if(!weapons.contains(toCheck.getType())) return;
		if(!toCheck.hasItemMeta()) return;
		
		ItemMeta meta = toCheck.getItemMeta();
		if(!meta.hasLore()) return;
		
		boolean hasPoison = false;
		for(String lore : meta.getLore()){
			if(lore.startsWith(poisonRecogString)){
				hasPoison = true;
				break;
			}
		}
		
		if(!hasPoison) return;
		
		OfflinePlayer player = Bukkit.getOfflinePlayer(event.getWhoClicked().getUniqueId());
		if(trait.getTraitHolder().getHolderManager().getHolderOfPlayer(player.getUniqueId()) 
				!= trait.getTraitHolder()){
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void OnPoisonOnWeaponBefore(PrepareItemCraftEvent event){
		Recipe recipe = event.getRecipe();
		for(Recipe ownRecipe : recipes){
			if(!ownRecipe.getResult().isSimilar(recipe.getResult())){
				continue;
			}
			
			for(ItemStack item : event.getInventory().getMatrix()){
				if(item == null) continue;
				if(item.getType() == PoisonItem) continue;
				if(item.getType() == Material.AIR) continue;
				
				ItemStack newItem = item.clone();

				ItemMeta meta = newItem.getItemMeta();
				List<String> lore = meta.hasLore() ? meta.getLore() : new LinkedList<String>();
				
				//Remove old poison.
				Iterator<String> loreIt = lore.iterator();
				while(loreIt.hasNext()){
					if(loreIt.next().startsWith(poisonRecogString)){
						loreIt.remove();
					}
				}
				
				lore.add(poisonRecogString + applications);
				meta.setLore(lore);
				
				newItem.setItemMeta(meta);
				event.getInventory().setResult(newItem);				
			}
		}
	}
	
	
	@EventHandler
	public void onItemDrop(PlayerDropItemEvent event){
		ItemStack item = event.getItemDrop().getItemStack();
		if(!item.hasItemMeta()) return;
		
		ItemMeta meta = item.getItemMeta();
		if(!meta.hasLore()) return;
		
		List<String> lore = meta.getLore();
		Iterator<String> loreIt = lore.iterator();
		while(loreIt.hasNext()){
			String currentLore = loreIt.next();
			if(currentLore.startsWith(poisonRecogString)){
				loreIt.remove();
				break;
			}
		}
		
		meta.setLore(lore);
		item.setItemMeta(meta);
	}
}