package com.massivecraft.factions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import com.massivecraft.factions.integration.Econ;

public class FWar{
	private static Set<FWar> wars = new HashSet<FWar>();
	
	public Set<InventoryView> tempInvs = new HashSet<InventoryView>();
	
	public Map<Material,Integer> items = new HashMap<Material,Integer>();
	public int money;
	
	public Faction attackerFaction, targetFaction;
	
	public boolean isStarted;
	public long time;
	
	
	public FWar(Faction attacker, Faction target){
		wars.add(this);
		
		this.attackerFaction=attacker;
		this.targetFaction=target;
		
		this.isStarted = false; // Obviously the war isn't started yet
	}
	
	public void startWar(){
		this.isStarted = true;
		
		/* Set the relation to enemy */
		//attackerFaction.setRelationWish(targetFaction, Relation.ENEMY);
		
		/* Set time */
		this.time = System.currentTimeMillis();
		
		/* Send the messages to both of the factions */
		targetFaction.sendMessage("Eurer Fraktion wurde eine Forderung gestellt, in höhe von "+getDemandsAsString());
		targetFaction.sendMessage("Die Forderung kam von "+attackerFaction.getTag()+" und falls ihr nicht innerhalb "+getTimeToWar()+" bezahlt, werden sie angreifen!");
		
		attackerFaction.sendMessage("Eure Fraktion hat "+targetFaction.getTag()+" Forderungen in höhe von "+getDemandsAsString()+" gestellt");
		attackerFaction.sendMessage("Falls sie nicht innerhalb "+getTimeToWar()+" zahlen, kommt es zum Krieg!");
		
	}
	
	public String getDemandsAsString(){
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
	
	public void addTempInventory(InventoryView inv){
		tempInvs.add(inv);
	}
	
	public void removeTempInventory(InventoryView inv){
		if(tempInvs.contains(inv)){
			for(ItemStack istack:inv.getTopInventory().getContents()){
				if(istack!=null){
					if(items.get(istack.getType())==null){
						items.put(istack.getType(), istack.getAmount());
					}
					else{
						items.put(istack.getType(), items.get(istack.getType())+istack.getAmount());
					}
				}
			}
			inv.getTopInventory().clear();
			tempInvs.remove(inv);
		}
	}
	
	public void remove(){
		wars.remove(this);
	}
	
	// Get functions
	public static Set<FWar> get(){
		return wars;
	}
	
	public static FWar get(Faction attackerFaction, Faction targetFaction){
		for(FWar war:wars){
			if(war.attackerFaction==attackerFaction){
				if(war.targetFaction==targetFaction){
					return war;
				}
			}
		}
		
		return null;
	}
	
	public static FWar getAsAttacker(Faction faction){
		for(FWar war:wars){
			if(war.attackerFaction==faction){
				return war;
			}
		}
		return null;
	}
	
	public static FWar getAsTarget(Faction faction){
		for(FWar war:wars){
			if(war.targetFaction==faction){
				return war;
			}
		}
		return null;
	}
	
	// Other static functions
	
	public static void removeFactionWars(Faction faction){
		for(FWar war:wars){
			if(war.attackerFaction==faction || war.targetFaction == faction){
				wars.remove(war);
			}
		}
	}
	
	public static FWar getWar(Faction attacker, Faction target){
		for(FWar war:wars){
			if(war.attackerFaction==attacker && war.targetFaction == target){
				return war;
			}
		}
		
		return null;
	}
}