package com.massivecraft.factions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import com.massivecraft.factions.integration.Econ;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.zcore.persist.Entity;

public class FWar extends Entity{	
	public transient Set<InventoryView> tempInvs;
	public transient Set<InventoryView> tempInvsFromTarget;
	
	public Map<Material,Integer[]> items = new HashMap<Material,Integer[]>();
	public int money;
	
	public Map<Material,Integer[]> itemsFromTarget = new HashMap<Material,Integer[]>();
	public int moneyFromTarget;
	
	private String attackerFactionID, targetFactionID;
	public Faction getAttackerFaction(){ return Factions.i.get(attackerFactionID);}
	public Faction getTargetFaction(){ return Factions.i.get(targetFactionID);}
	
	public boolean isStarted;
	public boolean isWar;
	public long time;
	public long timeToNextPayForMorePlayersThenTarget;
	
	public long timeToDeleteFWar;
	
	
	public FWar(Faction attacker, Faction target){
		this.attach();
		
		this.attackerFactionID=attacker.getId();
		this.targetFactionID=target.getId();
		
		this.isStarted = false; // Obviously the war isn't started yet
		this.isWar = false;//No War until time passed
		
		this.tempInvs = new HashSet<InventoryView>();
		this.tempInvsFromTarget = new HashSet<InventoryView>();
	}
	
	public void startWar(){
		this.isStarted = true;
		
		/* Set the relation to enemy */
		//attackerFaction.setRelationWish(targetFaction, Relation.ENEMY);
		
		/* Set time */
		this.time = System.currentTimeMillis();
		
		/* Send the messages to both of the factions */
		getTargetFaction().sendMessage("Eurer Fraktion wurde eine Forderung gestellt, in höhe von "+getDemandsAsString());
		getTargetFaction().sendMessage("Die Forderung kam von "+getAttackerFaction().getTag()+" und falls ihr nicht innerhalb "+getTimeToWar()+" bezahlt, werden sie angreifen!");
		
		getAttackerFaction().sendMessage("Eure Fraktion hat "+getTargetFaction().getTag()+" Forderungen in höhe von "+getDemandsAsString()+" gestellt");
		getAttackerFaction().sendMessage("Falls sie nicht innerhalb "+getTimeToWar()+" zahlen, kommt es zum Krieg!");
		
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
	
	
	public long getMilliTimeToWar(){
		long timeToWar=(24*60*60*1000)-(System.currentTimeMillis()-this.time);
		
		return timeToWar;
	}
	
	
	public long getMilliTimeToDeleteFWar(){
		long timeToDeleteFWar=(30*60*1000)-(System.currentTimeMillis()-this.timeToDeleteFWar);
		
		return timeToDeleteFWar;
	}
	
	
	public static void checkForDeleteFWars(){
		for(FWar war:FWars.i.get()){
			if(war.isStarted==false){
				if(war.getMilliTimeToDeleteFWar()<0){
					war.remove();
					war.getAttackerFaction().sendMessage("Kriegserklärungen gegen "+war.getTargetFaction().getTag()+" wurden abgebrochen da ihr länger als 30 Minuten gebraucht habt diese auszuarbeiten!");
				}
			}
		}
	}
	
	
	
	public static void setRelationshipWhenTimeToWarIsOver(){
		for(FWar war:FWars.i.get()){
			if(!war.isWar){
				if(war.getMilliTimeToWar()<=0){
					war.isWar=true;
					war.getAttackerFaction().setRelationWish(war.getTargetFaction(), Relation.ENEMY);
					war.getTargetFaction().setRelationWish(war.getAttackerFaction(), Relation.ENEMY);
					war.timeToNextPayForMorePlayersThenTarget = System.currentTimeMillis();
				}
			}
		}
	}
	
	
	public long getMilliTimeToNextPay(){
		long timeToPay=(24*60*60*1000)-(System.currentTimeMillis()-this.timeToNextPayForMorePlayersThenTarget);
		
		return timeToPay;
	}
	
	
	public static void payForMorePlayersThenTarget(){
		if(Conf.econEnabled){
			for(FWar war:FWars.i.get()){
				if(war.isWar){
					if(war.getMilliTimeToNextPay()<0){
						if(war.getAttackerFaction().getFPlayers().size()>war.getTargetFaction().getFPlayers().size()){
							int zwischenwert=war.getAttackerFaction().getFPlayers().size()-war.getTargetFaction().getFPlayers().size();
							if(Econ.modifyMoney(war.getAttackerFaction(), -(zwischenwert*10), "", "for paying the Playerdifference in a War")){
								
							}else{
								war.remove();
								war.getAttackerFaction().sendMessage("Der Krieg gegen "+war.getTargetFaction().getTag()+" wurde beendet da ihr kein Geld mehr habt um die Spielerdifferenz auszugleichen!");
								war.getTargetFaction().sendMessage("Der Krieg gegen "+war.getAttackerFaction().getTag()+" wurde beendet da sie kein Geld mehr habt um die Spielerdifferenz auszugleichen!");
							}
						}
						war.timeToNextPayForMorePlayersThenTarget = System.currentTimeMillis();
					}
				}
			}
		}
	}
	
	
	public void addTempInventory(InventoryView inv){
		tempInvs.add(inv);
	}
	
	public void removeTempInventory(InventoryView inv){
		if(tempInvs.contains(inv)){
			for(ItemStack istack:inv.getTopInventory().getContents()){
				if(istack!=null){
					Integer[] args= new Integer[2];
					if(items.get(istack.getType())==null){
						args[0]=(int) istack.getData().getData();
						args[1]=istack.getAmount();
						items.put(istack.getType(), args);
					}
					else{
						args[0]=(int) istack.getData().getData();
						Integer[] argsOLD = items.get(istack.getType());
						args[1]=argsOLD[1]+istack.getAmount();
						items.put(istack.getType(), args);
					}
				}
			}
			inv.getTopInventory().clear();
			tempInvs.remove(inv);
		}
	}
	
	public void addTempInventoryFromTarget(InventoryView inv){
		tempInvsFromTarget.add(inv);
	}
	
	public void removeTempInventoryFromTarget(InventoryView inv){
		if(tempInvsFromTarget.contains(inv)){
			for(ItemStack istack:inv.getTopInventory().getContents()){
				if(istack!=null){
					Integer[] args= new Integer[2];
					if(itemsFromTarget.get(istack.getType())==null){
						args[0]=(int) istack.getData().getData();
						args[1]=istack.getAmount();
						itemsFromTarget.put(istack.getType(), args);
					}
					else{
						args[0]=(int) istack.getData().getData();
						Integer[] argsOLD = itemsFromTarget.get(istack.getType());
						args[1]=argsOLD[1]+istack.getAmount();
						itemsFromTarget.put(istack.getType(), args);
					}
				}
			}
			inv.getTopInventory().clear();
			tempInvsFromTarget.remove(inv);
		}
	}
	
	public void remove(){
		FWars.i.detach(this);
		
		for(Material mat:items.keySet()){
			Integer[] args= new Integer[2];
			if(getAttackerFaction().factionInventory.get(mat)==null){
				getAttackerFaction().factionInventory.put(mat, items.get(mat));
			}else{
				args=items.get(mat);
				Integer[] argsOLD=getAttackerFaction().factionInventory.get(mat);
				args[1]=args[1]+ argsOLD[1];
				getAttackerFaction().factionInventory.put(mat, args);
				
			}
		}
		
		
		
		if(getAttackerFaction().getRelationTo(getTargetFaction())==Relation.ENEMY){
			getAttackerFaction().setRelationWish(getTargetFaction(), Relation.NEUTRAL);
			getTargetFaction().setRelationWish(getAttackerFaction(), Relation.NEUTRAL);
		}
		
		
	}
	
	// Get functions
	
	public static FWar get(Faction attackerFaction, Faction targetFaction){
		for(FWar war:FWars.i.get()){
			if(war.getAttackerFaction()==attackerFaction){
				if(war.getTargetFaction()==targetFaction){
					return war;
				}
			}
		}
		
		return null;
	}
	
	public static FWar getAsAttacker(Faction faction){
		for(FWar war:FWars.i.get()){
			if(war.getAttackerFaction()==faction){
				return war;
			}
		}
		return null;
	}
	
	public static FWar getAsTarget(Faction faction){
		for(FWar war:FWars.i.get()){
			if(war.getTargetFaction()==faction){
				return war;
			}
		}
		return null;
	}
	
	// Other static functions
	
	public static void removeFactionWars(Faction faction){
		for(FWar war:FWars.i.get()){
			if(war.getAttackerFaction()==faction || war.getTargetFaction() == faction){
				war.remove();
			}
		}
	}
	
	public static FWar getWar(Faction attacker, Faction target){
		for(FWar war:FWars.i.get()){
			if(war.getAttackerFaction()==attacker && war.getTargetFaction() == target){
				return war;
			}
		}
		
		return null;
	}
	
	
}