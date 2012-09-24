package com.massivecraft.factions.cmd;

import java.util.Collection;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.struct.Role;

public class CmdTaxation extends FCommand
{
	
	public CmdTaxation()
	{
		super();
		this.aliases.add("tax");
		this.aliases.add("t");
		
		this.optionalArgs.put("type","help");
		this.optionalArgs.put("param1","0");
		
		this.disableOnLock = true;
		
		senderMustBePlayer = true;
		senderMustBeMember = true;
		senderMustBeModerator = false;
		senderMustBeAdmin = false;
		
	}
	
	@Override
	public void perform()
	{
		// Read and validate input
		Faction forFaction = this.argAsFaction(0, myFaction);
		
		String command=this.argAsString(0);
		
		//The default Help Command
		if (command==null){
			command=new String("help");
		}
		
		//Adminberechtigungen
		boolean isAdmin=false;
		if(fme==forFaction.getFPlayerAdmin()||fme.isAdminBypassing()||forFaction.getFPlayersWhereRole(Role.MODERATOR).contains(fme)){
			isAdmin=true;
		}
		
		//Commands
		if (command.equals("set")){
			if(isAdmin){
				double tax=this.argAsDouble(1, 1d);
				
				boolean result=forFaction.setTax(tax);
				if (result){
					forFaction.msg("<i>The faction %s<i> have now a taxation from <b>%s<i> per hour", myFaction.getTag(forFaction), tax);
				}else{
					fme.msg("<b>The taxation must be < 100!");
				}
			}else{
				fme.msg("<b>You aren't faction Admin or Mod!%s",fme.getTag());
			}
		}else if(command.equals("get")){
			fme.msg("<i>The faction %s<i> has now a taxation from <b>%s<i> per hour", myFaction.getTag(forFaction), forFaction.getTax());
		}else if(command.equals("debts")){
			if(isAdmin){
				Collection<FPlayer> players = forFaction.getFPlayers();
				fme.msg(p.txt.titleize("Debts"));
				boolean havedebts=false;
				for (FPlayer follower : players)
				{
					
					if (follower.getTax()>0) {
						fme.msg("<i>%s<i>: <b>%s<i>", follower.getNameAndTitle(), follower.getTax());
						havedebts=true;
					}
				}
				if(!havedebts){
					fme.msg("<i>There are currently no debts to your Faction");
				}
			}else{
				fme.msg("<i>Your debts to the faction %s<i>: <b>%s<i>", myFaction.getTag(forFaction), fme.getTax());
			}
		}else if(command.equals("pay")){
			double ammount=this.argAsDouble(1, 1d);
			fme.payTax(ammount);
			fme.msg("<i>Your debts to the faction %s<i>: <b>%s<i>", myFaction.getTag(forFaction), fme.getTax());
		}else if(command.equals("erase")){
			if(isAdmin){
				String playername=this.argAsString(1);
				FPlayer player= FPlayers.i.get(playername);
				if (player.tax>0){player.tax=0;}
				fme.msg("<i>The depts from %s<i> are erased!",player.getName());
			}else{
				fme.msg("<b>You aren't faction Admin or Mod!%s",fme.getTag());
			}
		}else if(command.equals("help")){
			fme.msg(p.txt.titleize("Taxation Help"));
			fme.msg("&bf tax get <i>Show you the taxation");
			fme.msg("&bf tax debts <i>Show you your debts to the faction");
			fme.msg("&bf tax pay &3<amount> <i>Pay your debts");
			fme.msg("&bf tax help <i>Show this help page");
			if(isAdmin){
				fme.msg("&bf tax set &3<amount> <i>Set the taxation of your faction");
				fme.msg("&bf tax debts <i>See also the debts from the others");
				fme.msg("&bf tax erase &3<player> <i>Erase the debts of a player");
			}
			fme.msg("<i>You can also use 'f t' for the tax commands.");
		}else{
			fme.msg("<b>Command unknown. Press /f tax to see the help!");
		}
	}
	
}
