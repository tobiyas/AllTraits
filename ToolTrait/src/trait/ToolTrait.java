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
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

import de.tobiyas.racesandclasses.eventprocessing.eventresolvage.EventWrapper;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.TraitResults;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitConfigurationField;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitConfigurationNeeded;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitEventsUsed;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.annotations.configuration.TraitInfos;
import de.tobiyas.racesandclasses.traitcontainer.interfaces.markerinterfaces.Trait;
import de.tobiyas.racesandclasses.traitcontainer.traits.passive.AbstractPassiveTrait;
import de.tobiyas.racesandclasses.util.traitutil.TraitConfigurationFailedException;

public class ToolTrait extends AbstractPassiveTrait{

	@SuppressWarnings("unused")
	private boolean[] toolPerms;
	private String toolsPermsString;
	
	public ToolTrait(){
	}
	
	
	@TraitEventsUsed(registerdClasses = {PlayerInteractEvent.class})
	@Override
	public void generalInit(){
	}
	
	@Override
	public String getName() {
		return "ToolTrait";
	}


	@Override
	protected String getPrettyConfigIntern(){
		return toolsPermsString;
	}

	
	@TraitConfigurationNeeded(fields = {
			@TraitConfigurationField(fieldName = "forbid", classToExpect = String.class)
		})
	@Override
	public void setConfiguration(Map<String, Object> configMap) throws TraitConfigurationFailedException {
		super.setConfiguration(configMap);
		toolsPermsString = (String) configMap.get("forbid");
		toolPerms = readToolsUsable(toolsPermsString);
	}
	
	protected boolean[] readToolsUsable(String toolsUsable){
		boolean[] toolsUsableArray = new boolean[]{false, false, false, false, false, false};
		if(toolsUsable.contains("wood")){
			toolsUsableArray[0] = true;
		}
		
		if(toolsUsable.contains("stone")){
			toolsUsableArray[1] = true;
		}
		
		if(toolsUsable.contains("iron")){
			toolsUsableArray[2] = true;
		}
		
		if(toolsUsable.contains("gold")){
			toolsUsableArray[3] = true;
		}
		
		if(toolsUsable.contains("diamond")){
			toolsUsableArray[4] = true;
		}
		
		return toolsUsableArray;
	}
	

	@Override
	public TraitResults trigger(EventWrapper eventWrapper) {   Event event = eventWrapper.getEvent();
		if(!(event instanceof PlayerInteractEvent)) return TraitResults.False();
		//TODO implement me
		return TraitResults.False();
	}
	
	
	public static List<String> getHelpForTrait(){
		List<String> helpList = new LinkedList<String>();
		helpList.add(ChatColor.RED + "Nothing to see here yet.");
		return helpList;
	}
	
	
	@Override
	public boolean isBetterThan(Trait trait) {
		//TODO not implemented yet
		return true;
	}

	@TraitInfos(category="passive", traitName="ToolTrait", visible=true)
	@Override
	public void importTrait() {
	}


	@Override
	public boolean canBeTriggered(EventWrapper wrapper){
		// TODO Auto-generated method stub
		return true;
	}
}
