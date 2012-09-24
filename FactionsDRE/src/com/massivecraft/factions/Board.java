package com.massivecraft.factions;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.bukkit.ChatColor;
import com.google.gson.reflect.TypeToken;
import com.massivecraft.factions.integration.LWCFeatures;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.util.AsciiCompass;
import com.massivecraft.factions.zcore.util.DiscUtil;


public class Board
{
	private static transient File file = new File(P.p.getDataFolder(), "board.json");
	private static transient HashMap<FLocation, String> flocationIds = new HashMap<FLocation, String>();
	private static transient HashMap<FLocation, Integer> flocationPower = new HashMap<FLocation, Integer>();
	private static transient HashMap<FLocation, Integer> flocationToWildernessTimer = new HashMap<FLocation, Integer>();
	
	//----------------------------------------------//
	// Get and Set
	//----------------------------------------------//
	public static String getIdAt(FLocation flocation)
	{
		if ( ! flocationIds.containsKey(flocation))
		{
			return "0";
		}
		
		return flocationIds.get(flocation);
	}
	
	public static Faction getFactionAt(FLocation flocation)
	{
		return Factions.i.get(getIdAt(flocation));
	}
	
	public static void setIdAt(String id, FLocation flocation)
	{
		clearOwnershipAt(flocation);

		if (id == "0")
		{
			removeAt(flocation);
		}
		
		flocationIds.put(flocation, id);
		
		//Init Timer for NeutralZone
		if(id=="-3"){
			flocationToWildernessTimer.put(flocation, Conf.warNeutralZoneToWilderness);
		}
		
	}
	
	public static void setFactionAt(Faction faction, FLocation flocation)
	{
		setIdAt(faction.getId(), flocation);
	}
	
	public static void removeAt(FLocation flocation)
	{
		clearOwnershipAt(flocation);
		flocationIds.remove(flocation);
	}
	
	// not to be confused with claims, ownership referring to further member-specific ownership of a claim
	public static void clearOwnershipAt(FLocation flocation)
	{
		Faction faction = getFactionAt(flocation);
		if (faction != null && faction.isNormal())
		{
			faction.clearClaimOwnership(flocation);
		}
	}
	
	public static void unclaimAll(String factionId)
	{
		Faction faction = Factions.i.get(factionId);
		if (faction != null && faction.isNormal())
		{
			faction.clearAllClaimOwnership();
		}

		Iterator<Entry<FLocation, String>> iter = flocationIds.entrySet().iterator();
		while (iter.hasNext())
		{
			Entry<FLocation, String> entry = iter.next();
			if (entry.getValue().equals(factionId))
			{
				if(Conf.onUnclaimResetLwcLocks && LWCFeatures.getEnabled())
				{
					LWCFeatures.clearAllChests(entry.getKey());
				}
				iter.remove();
			}
		}
	}

	// Is this coord NOT completely surrounded by coords claimed by the same faction?
	// Simpler: Is there any nearby coord with a faction other than the faction here?
	public static boolean isBorderLocation(FLocation flocation)
	{
		Faction faction = getFactionAt(flocation);
		FLocation a = flocation.getRelative(1, 0);
		FLocation b = flocation.getRelative(-1, 0);
		FLocation c = flocation.getRelative(0, 1);
		FLocation d = flocation.getRelative(0, -1);
		return faction != getFactionAt(a) || faction != getFactionAt(b) || faction != getFactionAt(c) || faction != getFactionAt(d);
	}

	// Is this coord connected to any coord claimed by the specified faction?
	public static boolean isConnectedLocation(FLocation flocation, Faction faction)
	{
		FLocation a = flocation.getRelative(1, 0);
		FLocation b = flocation.getRelative(-1, 0);
		FLocation c = flocation.getRelative(0, 1);
		FLocation d = flocation.getRelative(0, -1);
		return faction == getFactionAt(a) || faction == getFactionAt(b) || faction == getFactionAt(c) || faction == getFactionAt(d);
	}
	
	
	//----------------------------------------------//
	// Cleaner. Remove orphaned foreign keys
	//----------------------------------------------//
	
	public static void clean()
	{
		Iterator<Entry<FLocation, String>> iter = flocationIds.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<FLocation, String> entry = iter.next();
			if ( ! Factions.i.exists(entry.getValue()))
			{
				if(Conf.onUnclaimResetLwcLocks && LWCFeatures.getEnabled())
				{
					LWCFeatures.clearAllChests(entry.getKey());
				}
				P.p.log("Board cleaner removed "+entry.getValue()+" from "+entry.getKey());
				iter.remove();
			}
		}
	}	
	
	//----------------------------------------------//
	// Coord count
	//----------------------------------------------//
	
	public static int getFactionCoordCount(String factionId)
	{
		int ret = 0;
		for (String thatFactionId : flocationIds.values())
		{
			if(thatFactionId.equals(factionId))
			{
				ret += 1;
			}
		}
		return ret;
	}
	
	public static int getFactionCoordCount(Faction faction)
	{
		return getFactionCoordCount(faction.getId());
	}
	
	public static int getFactionCoordCountInWorld(Faction faction, String worldName)
	{
		String factionId = faction.getId();
		int ret = 0;
		Iterator<Entry<FLocation, String>> iter = flocationIds.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<FLocation, String> entry = iter.next();
			if (entry.getValue().equals(factionId) && entry.getKey().getWorldName().equals(worldName))
			{
				ret += 1;
			}
		}
		return ret;
	}
	
	//----------------------------------------------//
	// FactionGrenze check
	//----------------------------------------------//
	
	public static Boolean isFactionGrenzeAt(FLocation flocation)
	{
		if ( ! flocationIds.containsKey(flocation))
		{
			return false;
		}
		
		String factionId=getIdAt(flocation);
		
		if(
				!getIdAt(flocation.getRelative(0, 1)).equals(factionId) || 
				!getIdAt(flocation.getRelative(0, -1)).equals(factionId)||
				!getIdAt(flocation.getRelative(-1, 0)).equals(factionId)||
				!getIdAt(flocation.getRelative(1, 0)).equals(factionId))
		{
			return true;
		}
		
		return false;
	}
	
	//----------------------------------------------//
	// Power
	//----------------------------------------------//
	public static int incrasePower(FLocation flocation, int power){
		if(!flocationPower.containsKey(flocation)){
			flocationPower.put(flocation, Conf.warConquestTime);
		}
		
		int changedpower=flocationPower.get(flocation)+power;
		
		if(changedpower>Conf.warConquestTime){
			changedpower=Conf.warConquestTime;
		}
		
		if(changedpower<1){
			setIdAt("-3",flocation); //Neutral setzen
			flocationPower.remove(flocation);
			return 0;
		}
		
		flocationPower.put(flocation,changedpower);
		return changedpower;
	}
	
	//----------------------------------------------//
	// Update NeutralZone
	//----------------------------------------------//
	public static void updateNZone(){
		for(FLocation location:flocationToWildernessTimer.keySet()){
			int time=flocationToWildernessTimer.get(location);
			
			time--;
			
			//Set to Wilderness
			if(time<=0){
				setIdAt("0",location);
				flocationToWildernessTimer.remove(location);
			}else{
				flocationToWildernessTimer.put(location,time);
			}
		}
	}
	
	//----------------------------------------------//
	// Map generation
	//----------------------------------------------//
	
	/**
	 * The map is relative to a coord and a faction
	 * north is in the direction of decreasing x
	 * east is in the direction of decreasing z
	 */
	
	public static ArrayList<String> getMap(Faction faction, FLocation flocation, double inDegrees)
	{
		ArrayList<String> ret = new ArrayList<String>();
		Faction factionLoc = getFactionAt(flocation);
		ret.add(P.p.txt.titleize("("+flocation.getCoordString()+") "+factionLoc.getTag(faction)));
		
		int halfWidth = Conf.mapWidth / 2;
		int halfHeight = Conf.mapHeight / 2;
		FLocation topLeft = flocation.getRelative(-halfWidth, -halfHeight);
		int width = halfWidth * 2 + 1;
		int height = halfHeight * 2 + 1;
		
		if (Conf.showMapFactionKey)
		{
			height--;
		}
		
		Map<String, Character> fList = new HashMap<String, Character>();
		int chrIdx = 0;
		
		// For each row
		for (int dz = 0; dz < height; dz++)
		{
			// Draw and add that row
			String row = "";
			for (int dx = 0; dx < width; dx++)
			{
				if(dx == halfWidth && dz == halfHeight)
				{
					row += ChatColor.AQUA+"+";
				}
				else
				{
					FLocation flocationHere = topLeft.getRelative(dx, dz);
					Faction factionHere = getFactionAt(flocationHere);
					Relation relation = faction.getRelationTo(factionHere);
					if (factionHere.isNone())
					{
						row += ChatColor.GRAY+"-";
					}
					else if (factionHere.isSafeZone())
					{
						row += Conf.colorPeaceful+"+";
					}
					else if (factionHere.isWarZone())
					{
						row += ChatColor.DARK_RED+"+";
					}
					else if
					(
						factionHere == faction
						||
						factionHere == factionLoc
						||
						relation.isAtLeast(Relation.ALLY)
						||
						(Conf.showNeutralFactionsOnMap && relation.equals(Relation.NEUTRAL))
						||
						(Conf.showEnemyFactionsOnMap && relation.equals(Relation.ENEMY))
					)
					{
						if (!fList.containsKey(factionHere.getTag()))
							fList.put(factionHere.getTag(), Conf.mapKeyChrs[chrIdx++]);
						char tag = fList.get(factionHere.getTag());
						row += factionHere.getColorTo(faction) + "" + tag;
					}
					else
					{
						row += ChatColor.GRAY+"-";
					}
				}
			}
			ret.add(row);
		}
		
		// Get the compass
		ArrayList<String> asciiCompass = AsciiCompass.getAsciiCompass(inDegrees, ChatColor.RED, P.p.txt.parse("<a>"));

		// Add the compass
		ret.set(1, asciiCompass.get(0)+ret.get(1).substring(3*3));
		ret.set(2, asciiCompass.get(1)+ret.get(2).substring(3*3));
		ret.set(3, asciiCompass.get(2)+ret.get(3).substring(3*3));
		
		// Add the faction key
		if (Conf.showMapFactionKey)
		{
			String fRow = "";
			for(String key : fList.keySet())
			{
				fRow += String.format("%s%s: %s ", ChatColor.GRAY, fList.get(key), key);
			}
			ret.add(fRow);
		}
		
		return ret;
	}
	
	

	
	// -------------------------------------------- //
	// Persistance
	// -------------------------------------------- //
	
	public static Map<String,Map<String,Map<String,String>>> dumpAsSaveFormat()
	{
		Map<String,Map<String,Map<String,String>>> worldCoordIds = new HashMap<String,Map<String,Map<String,String>>>(); 
		
		String worldName, coords;
		String id, timer;
		
		for (Entry<FLocation, String> entry : flocationIds.entrySet())
		{
			worldName = entry.getKey().getWorldName();
			coords = entry.getKey().getCoordString();
			id = entry.getValue();
			if ( ! worldCoordIds.containsKey(worldName))
			{
				worldCoordIds.put(worldName, new TreeMap<String,Map<String,String>>());
			}
			
			worldCoordIds.get(worldName).put(coords, new TreeMap<String,String>());
			
			//NeutralZone Timer
			timer="0";
			if(flocationToWildernessTimer.get(entry.getKey())!=null){
				timer=flocationToWildernessTimer.get(entry.getKey()).toString();
			}
			
			worldCoordIds.get(worldName).get(coords).put(id, timer);
		}
		
		return worldCoordIds;
	}
	
	public static void loadFromSaveFormat(Map<String,Map<String,Map<String,String>>> worldCoordIds)
	{
		flocationIds.clear();
		
		String worldName;
		String[] coords;
		int x, z;
		String factionId;
		int timer;
		
		for (Entry<String, Map<String, Map<String, String>>> entry : worldCoordIds.entrySet())
		{
			worldName = entry.getKey();
			for (Entry<String, Map<String, String>> entry2 : entry.getValue().entrySet())
			{
				coords = entry2.getKey().trim().split("[,\\s]+");
				x = Integer.parseInt(coords[0]);
				z = Integer.parseInt(coords[1]);
				for (Entry<String,String> entry3 : entry2.getValue().entrySet())
				{
					factionId = entry3.getKey();
					timer = Integer.parseInt(entry3.getValue());
					FLocation tmpLocation=new FLocation(worldName, x, z);
					flocationIds.put(tmpLocation, factionId);
					if(timer>0){
						flocationToWildernessTimer.put(tmpLocation, timer);
					}
				}
			}
		}
	}
	
	//Function wenn das alte Boardfile veraltet ist
	public static void loadFromSaveFormat(Map<String,Map<String,String>> worldCoordIds, boolean i)
	{
		flocationIds.clear();
		
		String worldName;
		String[] coords;
		int x, z;
		String factionId;
		
		for (Entry<String, Map<String, String>> entry : worldCoordIds.entrySet())
		{
			worldName = entry.getKey();
			for (Entry<String, String> entry2 : entry.getValue().entrySet())
			{
				coords = entry2.getKey().trim().split("[,\\s]+");
				x = Integer.parseInt(coords[0]);
				z = Integer.parseInt(coords[1]);
				factionId = entry2.getValue();
				FLocation tmpLocation=new FLocation(worldName, x, z);
				flocationIds.put(tmpLocation, factionId);
			}
		}
	}
	
	public static boolean save()
	{
		//Factions.log("Saving board to disk");
		
		try
		{
			DiscUtil.write(file, P.p.gson.toJson(dumpAsSaveFormat()));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			P.p.log("Failed to save the board to disk.");
			return false;
		}
		
		return true;
	}
	
	public static boolean load()
	{
		P.p.log("Loading board from disk");
		
		if ( ! file.exists())
		{
			P.p.log("No board to load from disk. Creating new file.");
			save();
			return true;
		}
		
		try
		{
			try{
				Type type = new TypeToken<Map<String,Map<String,Map<String,String>>>>(){}.getType();
				Map<String,Map<String,Map<String,String>>> worldCoordIds = P.p.gson.fromJson(DiscUtil.read(file), type);
				loadFromSaveFormat(worldCoordIds);
			}
			catch (Exception e){
				Type type = new TypeToken<Map<String,Map<String,String>>>(){}.getType();
				Map<String,Map<String,String>> worldCoordIds = P.p.gson.fromJson(DiscUtil.read(file), type);
				loadFromSaveFormat(worldCoordIds, true);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			P.p.log("Failed to load the board from disk.");
			return false;
		}
			
		return true;
	}
}



















