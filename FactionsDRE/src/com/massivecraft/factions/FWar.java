package com.massivecraft.factions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import com.massivecraft.factions.integration.Econ;

public class FWar{
	public static Set<FWar> wars = new HashSet<FWar>();
	
	public Map<Material,Integer> items = new HashMap<Material,Integer>();
	public Set<Chest> chests;
	public int money;
	
	public Faction attacker,target;
	
	public long time;
	
	
	public FWar(Faction attacker, Faction target, Set<Chest> chests, int money){
		wars.add(this);
		
		this.attacker=attacker;
		this.target=target;
		
		this.chests=chests;
		
		for(Chest chest:chests){
			for(ItemStack istack:chest.getInventory().getContents()){
				if(istack!=null){
					if(items.get(istack.getType())==null){
						items.put(istack.getType(), istack.getAmount());
					}
					
					else{
						items.put(istack.getType(), items.get(istack.getType())+istack.getAmount());
					}
				}
			}
		}
		
		this.money=money;
		
		this.time=System.currentTimeMillis();
		
		target.sendMessage("Eurer Fraktion wurde eine Forderung gestellt, in höhe von "+getItemsAsString());
		target.sendMessage("Die Forderung kam von "+attacker.getTag()+" und falls ihr nicht innerhalb "+getTimeToWar()+" bezahlt, werden sie angreifen!");
		
		attacker.sendMessage("Eure Fraktion hat "+target.getTag()+" Forderungen in höhe von "+getItemsAsString()+" gestellt");
		attacker.sendMessage("Falls sie nicht innerhalb "+getTimeToWar()+" zahlen, kommt es zum Krieg!");
	}
	
	public String getItemsAsString(){
		String ausgabe="";
		
		if(Conf.econEnabled){
			if(this.money>0)
				ausgabe=ausgabe+Econ.moneyString(this.money)+", ";
		}
		
		int i=0;
		for(Material item:items.keySet()){
			i++;
			if(i>1 && i < items.size()) ausgabe=ausgabe+", ";
			
			if(i==items.size()) ausgabe=ausgabe+" und ";
			
			ausgabe=ausgabe+items.get(item)+" "+item.name();
		}
		
		return ausgabe;
	}
	
	public String getTimeToWar(){
		long timeToWar=(24*60*60*1000)-(System.currentTimeMillis()-this.time);
		
		return (int)Math.floor(timeToWar/(60*60*1000))+"h "+(int)Math.floor(timeToWar%(60*60*1000)/(60*1000))+"min";
	}
	
	public static FWar getAsAttacker(Faction faction){
		for(FWar war:wars){
			if(war.attacker==faction){
				return war;
			}
		}
		return null;
	}
	
	public static FWar getAsTarget(Faction faction){
		for(FWar war:wars){
			if(war.target==faction){
				return war;
			}
		}
		return null;
	}
	
	//Statics
	public static void removeFactionWars(Faction faction){
		for(FWar war:wars){
			if(war.attacker==faction || war.target == faction){
				wars.remove(war);
			}
		}
	}
	
	public static FWar getWar(Faction attacker, Faction target){
		for(FWar war:wars){
			if(war.attacker==attacker && war.target == target){
				return war;
			}
		}
		
		return null;
	}
	
	public static boolean isWarChest(Chest chest){
		for(FWar war:wars){
			for(Chest warChest:war.chests){
				if(warChest.equals(chest)){
					return true;
				}
			}
		}
		return false;
	}
}