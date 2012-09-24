package com.massivecraft.factions.cmd;

import org.bukkit.ChatColor;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FWar;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.factions.struct.Permission;

public class CmdWar extends FCommand{
	//Befehl um einen Krieg zu starten mit Forderungen
	
	public CmdWar()
	{
		super();
		this.aliases.add("war");
		
		this.requiredArgs.add("target name");
		this.optionalArgs.put("money", "-1");
		
		this.permission = Permission.MOD.node;
		this.disableOnLock = true;
		
		senderMustBePlayer = false;
		senderMustBeMember = false;
		senderMustBeModerator = false;
		senderMustBeAdmin = false;
	}
	
	@Override
	public void perform() {
		if(!fme.isMakingForderungen){
			Faction faction=this.argAsFaction(0);
			
			
			if(faction!=null){
				if(faction.isNormal()){
					me.sendMessage(ChatColor.GOLD+"Du bist dabei einen Krieg gegen die Fraktion "+ChatColor.GREEN+faction.getTag()+ChatColor.GOLD+" zu starten!");
					me.sendMessage(ChatColor.GOLD+"Du musst nun Kisten markieren indem du Links auf sie raufklickst. Diese Kisten werden während dem Krieg gesperrt. Der Inhalt der Kisten sind die Forderungen an die andere Fraktion.");
					if(Conf.econEnabled){
						me.sendMessage(ChatColor.GOLD+"Zusätzlich kann man auch Geld zu den Forderungen hinzufügen: "+ChatColor.GREEN+"/f war "+faction.getTag()+" money");
					}
					me.sendMessage(ChatColor.GOLD+"Falls du fertig bist muss du den Befehl "+ChatColor.GREEN+"/f war "+faction.getTag()+ChatColor.GOLD+" wiederholen");
					
					fme.isMakingForderungen=true;
					fme.ForderungsFraktion=faction;
					fme.ForderungsKisten.clear();
					fme.ForderungsMoney=0;
					
				}else{
					me.sendMessage(ChatColor.RED+"Du kannst nur Krieg gegen "+ChatColor.GOLD+faction.getTag()+ChatColor.RED+" starten!");
				}
			}
			else{
				me.sendMessage(ChatColor.RED+"Die Fraktion "+ChatColor.GOLD+this.argAsString(0)+ChatColor.RED+" existiert nicht!");
			}
		}
		
		else{
			//Money
			if(Conf.econEnabled){
				if(this.argIsSet(1)){
					int money=this.argAsInt(1);
					
					if(money>=0){
						int newMoney=money-fme.ForderungsMoney;
						
						if(newMoney<=0){
							Econ.deposit(myFaction.getAccountId(), -newMoney);
							fme.ForderungsMoney=money;
							me.sendMessage(ChatColor.GOLD+"Der Forderungsbetrag wurde auf "+ChatColor.GREEN+fme.ForderungsMoney+ChatColor.GOLD+" gesetzt");
						}
						else{
							if(Econ.getBalance(myFaction.getAccountId())<newMoney){
								me.sendMessage(ChatColor.RED+"Deine Fraktion hat zuwenig Geld um eine solche Forderung stellen zu können!");
							}
							else{
								Econ.withdraw(myFaction.getAccountId(),newMoney);
								fme.ForderungsMoney=money;
								me.sendMessage(ChatColor.GOLD+"Der Forderungsbetrag wurde auf "+ChatColor.GREEN+fme.ForderungsMoney+ChatColor.GOLD+" gesetzt");
							}
						}
					}
					else{
						me.sendMessage(ChatColor.RED+"Der Betrag muss gleich oder mehr als 0 sein!");
					}
					return;
				}
			}
			//Beginn War
			if(this.argAsFaction(0)==fme.ForderungsFraktion){
				new FWar(myFaction, fme.ForderungsFraktion, fme.ForderungsKisten, fme.ForderungsMoney);
				fme.ForderungsFraktion=null;
				fme.ForderungsKisten.clear();
				fme.ForderungsMoney=0;
				fme.isMakingForderungen=false;
			}else{
				me.sendMessage(ChatColor.RED+"Du bist momentan daran einen Krieg gegen "+ChatColor.GOLD+fme.ForderungsFraktion.getTag()+ChatColor.RED+" zu starten, bitte schliess diese Aktion zuerst ab!");
			}
			
		}
	}
	
}
