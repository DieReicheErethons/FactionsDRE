package com.massivecraft.factions.listeners;

import java.util.logging.Logger;
import java.util.Iterator;
import java.util.List;
import java.util.UnknownFormatConversionException;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.getspout.spoutapi.SpoutManager;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.FWar;
import com.massivecraft.factions.FWars;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.P;
import com.massivecraft.factions.buildings.BuildingType;
import com.massivecraft.factions.integration.LWCFeatures;
import com.massivecraft.factions.integration.spout.SpoutFeatures;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.zcore.util.TextUtil;

import java.util.logging.Level;



public class FactionsPlayerListener implements Listener
{
	public P p;
	public FactionsPlayerListener(P p)
	{
		this.p = p;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat(AsyncPlayerChatEvent event)
	{
		if (event.isCancelled()) return;
		
		Player talkingPlayer = event.getPlayer();
		String msg = event.getMessage();
		
		// ... it was not a command. This means that it is a chat message!
		FPlayer me = FPlayers.i.get(talkingPlayer);
		
		// Are we to insert the Faction tag into the format?
		// If we are not to insert it - we are done.
		if ( ! Conf.chatTagEnabled || Conf.chatTagHandledByAnotherPlugin)
		{
			return;
		}

		int InsertIndex = 0;
		String eventFormat = event.getFormat();
		
		if (!Conf.chatTagReplaceString.isEmpty() && eventFormat.contains(Conf.chatTagReplaceString))
		{
			// we're using the "replace" method of inserting the faction tags
			// if they stuck "[FACTION_TITLE]" in there, go ahead and do it too
			if (eventFormat.contains("[FACTION_TITLE]"))
			{
				eventFormat = eventFormat.replace("[FACTION_TITLE]", me.getTitle());
			}
			InsertIndex = eventFormat.indexOf(Conf.chatTagReplaceString);
			eventFormat = eventFormat.replace(Conf.chatTagReplaceString, "");
			Conf.chatTagPadAfter = false;
			Conf.chatTagPadBefore = false;
		}
		else if (!Conf.chatTagInsertAfterString.isEmpty() && eventFormat.contains(Conf.chatTagInsertAfterString))
		{
			// we're using the "insert after string" method
			InsertIndex = eventFormat.indexOf(Conf.chatTagInsertAfterString) + Conf.chatTagInsertAfterString.length();
		}
		else if (!Conf.chatTagInsertBeforeString.isEmpty() && eventFormat.contains(Conf.chatTagInsertBeforeString))
		{
			// we're using the "insert before string" method
			InsertIndex = eventFormat.indexOf(Conf.chatTagInsertBeforeString);
		}
		else
		{
			// we'll fall back to using the index place method
			InsertIndex = Conf.chatTagInsertIndex;
			if (InsertIndex > eventFormat.length())
				return;
		}
		
		String formatStart = eventFormat.substring(0, InsertIndex) + ((Conf.chatTagPadBefore && !me.getChatTag().isEmpty()) ? " " : "");
		String formatEnd = ((Conf.chatTagPadAfter && !me.getChatTag().isEmpty()) ? " " : "") + eventFormat.substring(InsertIndex);
		
		String nonColoredMsgFormat = formatStart + me.getChatTag().trim() + formatEnd;
		
		// Relation Colored?
		if (Conf.chatTagRelationColored)
		{
			// We must choke the standard message and send out individual messages to all players
			// Why? Because the relations will differ.
			event.setCancelled(true);
			
			for (Player listeningPlayer : event.getRecipients())
			{
				FPlayer you = FPlayers.i.get(listeningPlayer);
				String yourFormat = formatStart + me.getChatTag(you).trim() + formatEnd;
				try
				{
					listeningPlayer.sendMessage(String.format(yourFormat, talkingPlayer.getDisplayName(), msg));
				}
				catch (UnknownFormatConversionException ex)
				{
					Conf.chatTagInsertIndex = 0;
					P.p.log(Level.SEVERE, "Critical error in chat message formatting!");
					P.p.log(Level.SEVERE, "NOTE: This has been automatically fixed right now by setting chatTagInsertIndex to 0.");
					P.p.log(Level.SEVERE, "For a more proper fix, please read this regarding chat configuration: http://massivecraft.com/plugins/factions/config#Chat_configuration");
					return;
				}
			}
			
			// Write to the log... We will write the non colored message.
			String nonColoredMsg = ChatColor.stripColor(String.format(nonColoredMsgFormat, talkingPlayer.getDisplayName(), msg));
			Logger.getLogger("Minecraft").info(nonColoredMsg);
		}
		else
		{
			// No relation color.
			event.setFormat(nonColoredMsgFormat);
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		// Make sure that all online players do have a fplayer.
		final FPlayer me = FPlayers.i.get(event.getPlayer());
		
		// Update the lastLoginTime for this fplayer
		me.setLastLoginTime(System.currentTimeMillis());
		
		// Taxation  by Frank
		me.taxoldtime=0;
		me.getTax();
		
		// Spout  by Frank
		if(SpoutFeatures.enabled()){
			me.setsPlayer(SpoutManager.getPlayer(event.getPlayer()));
			me.initSpoutMenu();
		}
		
		// FWar by Frank
		if(me.getFaction()!=null){
			FWar war=FWar.getAsTarget(me.getFaction());
			if(war!=null){
				me.sendMessage("Eurer Fraktion wurde eine Forderung gestellt, in h�he von "+war.getDemandsAsString());
				me.sendMessage("Die Forderung kam von "+war.getAttackerFaction().getTag()+" und falls ihr nicht innerhalb "+war.getTimeToWar()+" bezahlt, werden sie angreifen!");
			}
		}

		SpoutFeatures.updateAppearancesShortly(event.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event)
    {
		FPlayer me = FPlayers.i.get(event.getPlayer());

		// Make sure player's power is up to date when they log off.
		me.getPower();
		
		// and update their last login time to point to when the logged off, for auto-remove routine
		me.setLastLoginTime(System.currentTimeMillis());
		
		Faction myFaction = me.getFaction();
		if (myFaction != null)
		{
			myFaction.memberLoggedOff();
		}
		SpoutFeatures.playerDisconnect(me);
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerMove(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();
		FPlayer me = FPlayers.i.get(player);
		
		// Did we change coord?
		FLocation from = me.getLastStoodAt();
		FLocation to = new FLocation(player.getLocation());
		
		if (from.equals(to))
		{
			return;
		}
		
		// Yes we did change coord (:
		
		me.setLastStoodAt(to);

		// Did we change "host"(faction)?
		boolean spoutClient = SpoutFeatures.availableFor(player);
		Faction factionFrom = Board.getFactionAt(from);
		Faction factionTo = Board.getFactionAt(to);
		boolean changedFaction = (factionFrom != factionTo);

		//Did we change Factiongrenze?
		
		boolean  GrenzeFrom=Board.isFactionGrenzeAt(from);
		boolean  GrenzeTo=Board.isFactionGrenzeAt(to);
		boolean changedFactionGrenze= (GrenzeFrom!=GrenzeTo)&&(factionFrom==factionTo);
		if(!changedFactionGrenze){
			changedFactionGrenze=(factionFrom!=factionTo);
		}
		
		//Is our Faction in War?
		boolean isFactionInWar=false;
		for(Faction faction:Factions.i.get()){
			if(faction.getRelationTo(me.getFaction())==Relation.ENEMY){
				if(me.getFaction()==factionTo){
					isFactionInWar=true;
					break;
				}
				else if(factionTo.getRelationTo(me.getFaction())==Relation.ENEMY){
					isFactionInWar=true;
					break;
				}
			}
		}
		
		
		if(changedFactionGrenze&&isFactionInWar){
			if(GrenzeFrom){
				me.sendMessage(ChatColor.GOLD+"Du bist nun im "+ChatColor.GREEN+"sicheren Gebiet");
			}else if(GrenzeTo){
				me.sendMessage(ChatColor.GOLD+"Du bist nun im "+ChatColor.RED+"Grenzgebiet");
			}
		}
		
		
		if (changedFaction && SpoutFeatures.updateTerritoryDisplay(me))
			changedFaction = false;

		if (me.isMapAutoUpdating())
		{
			me.sendMessage(Board.getMap(me.getFaction(), to, player.getLocation().getYaw()));

			if (spoutClient && Conf.spoutTerritoryOwnersShow)
				SpoutFeatures.updateOwnerList(me);
		}
		else
		{
			Faction myFaction = me.getFaction();
			String ownersTo = myFaction.getOwnerListString(to);

			if (changedFaction)
			{
				me.sendFactionHereMessage();
				if
				(
					Conf.ownedAreasEnabled
					&&
					Conf.ownedMessageOnBorder
					&&
					(
						!spoutClient
						||
						!Conf.spoutTerritoryOwnersShow
					)
					&&
					myFaction == factionTo
					&&
					!ownersTo.isEmpty()
				)
				{
					me.sendMessage(Conf.ownedLandMessage+ownersTo);
				}
				
				if(factionTo.isNormal()&&isFactionInWar){
					if(Board.isFactionGrenzeAt(to)){
						me.sendMessage(ChatColor.GOLD+"Du bist nun im "+ChatColor.RED+"Grenzgebiet");
					}else{
						me.sendMessage(ChatColor.GOLD+"Du bist nun im "+ChatColor.GREEN+"sicheren Gebiet");
					}
				}
			}
			else if (spoutClient && Conf.spoutTerritoryOwnersShow)
			{
				SpoutFeatures.updateOwnerList(me);
			}
			else if
			(
				Conf.ownedAreasEnabled
				&&
				Conf.ownedMessageInsideTerritory
				&&
				factionFrom == factionTo
				&&
				myFaction == factionTo
			)
			{
				String ownersFrom = myFaction.getOwnerListString(from);
				if (Conf.ownedMessageByChunk || !ownersFrom.equals(ownersTo))
				{
					if (!ownersTo.isEmpty())
						me.sendMessage(Conf.ownedLandMessage+ownersTo);
					else if (!Conf.publicLandMessage.isEmpty())
						me.sendMessage(Conf.publicLandMessage);
				}
			}
		}
		
		if (me.getAutoClaimFor() != null)
		{
			me.attemptClaim(me.getAutoClaimFor(), player.getLocation(), true);
		}
		else if (me.isAutoSafeClaimEnabled())
		{
			if ( ! Permission.MANAGE_SAFE_ZONE.has(player))
			{
				me.setIsAutoSafeClaimEnabled(false);
			}
			else
			{
				FLocation playerFlocation = new FLocation(me);

				if (!Board.getFactionAt(playerFlocation).isSafeZone())
				{
					Board.setFactionAt(Factions.i.getSafeZone(), playerFlocation);
					me.msg("<i>This land is now a safe zone.");
				}
			}
		}
		else if (me.isAutoWarClaimEnabled())
		{
			if ( ! Permission.MANAGE_WAR_ZONE.has(player))
			{
				me.setIsAutoWarClaimEnabled(false);
			}
			else
			{
				FLocation playerFlocation = new FLocation(me);

				if (!Board.getFactionAt(playerFlocation).isWarZone())
				{
					Board.setFactionAt(Factions.i.getWarZone(), playerFlocation);
					me.msg("<i>This land is now a war zone.");
				}
			}
		}
		
		
		
		//Buildings  by Frank
		if(me.getIsbuilding()!=null){
			
			Location seelocation=null;
			
			
			List<Block> sight = me.getPlayer().getLastTwoTargetBlocks(null, 30);
			
			for(Block block:sight){
				if(!BuildingType.isIgnoredBlock((byte)block.getTypeId())){
					seelocation=block.getLocation();
					break;
				}
			}
			
			if(me.getLastseelocation()==null){
				me.setLastseelocation(seelocation);
			}
			
			//Ist ein block in sicht?
			if (seelocation!=null){
				//Ticker schon abgelaufen?
				if(me.getBuildingticker()<System.currentTimeMillis()){
					// Did we change coord?
					if (me.getLastseelocation().getBlockX()!=seelocation.getBlockX() || me.getLastseelocation().getBlockY()!=seelocation.getBlockY() || me.getLastseelocation().getBlockZ()!=seelocation.getBlockZ()){
						//Yes we did :)
							me.getIsbuilding().checkBuilding(me, seelocation.getBlockX(), seelocation.getBlockY(), seelocation.getBlockZ(), 1);
							
							//Location updaten
							me.setLastseelocation(seelocation);
							
							//Ticker einstellen
							me.setBuildingticker(System.currentTimeMillis()+300);
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
		
		Block block = event.getClickedBlock();
		Player player = event.getPlayer();
		if(block!=null){
			
			
			if(LWCFeatures.getEnabled()){
				FPlayer me = FPlayers.i.get(player);
				
				FLocation loc = new FLocation(block);
				Faction otherFaction = Board.getFactionAt(loc);
				
				if(me.getFaction()==otherFaction){
					if(me.getRole()==Role.ADMIN){
						if(event.isCancelled()){
							event.setCancelled(false);
							
						}
						p.log("LWC-Bypass for factions-Admin");
						//LWCFeatures.get().enforceAccess(player, LWCFeatures.get().findProtection(block), block);
					}
				}
			}
		}
		
		
		if (event.isCancelled()) return;

		

		if (block == null)
		{
			return;  // clicked in air, apparently
		}

		if ( ! canPlayerUseBlock(player, block, false))
		{
			event.setCancelled(true);
			return;
		}
		
		
		
		
		
		
		
		
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
		{
			return;  // only interested on right-clicks for below
		}

		// workaround fix for new CraftBukkit 1.1-R1 bug where half-step on half-step placement doesn't trigger BlockPlaceEvent
		if (
				event.hasItem()
				&&
				event.getItem().getType() == Material.STEP
				&&
				block.getType() == Material.STEP
				&&
				event.getBlockFace() == BlockFace.UP
				&&
				event.getItem().getData().getData() == block.getData()
				&&
				! FactionsBlockListener.playerCanBuildDestroyBlock(player, block, "build", false)
			)
		{
			event.setCancelled(true);
			return;
		}

		if ( ! playerCanUseItemHere(player, block.getLocation(), event.getMaterial(), false))
		{
			event.setCancelled(true);
			return;
		}
		
		
		
	}

	public static boolean playerCanUseItemHere(Player player, Location location, Material material, boolean justCheck)
	{
		FPlayer me = FPlayers.i.get(player);
		if (me.isAdminBypassing())
			return true;

		FLocation loc = new FLocation(location);
		Faction otherFaction = Board.getFactionAt(loc);

		if (otherFaction.hasPlayersOnline())
		{
			if ( ! Conf.territoryDenyUseageMaterials.contains(material))
				return true; // Item isn't one we're preventing for online factions.
		}
		else
		{
			if ( ! Conf.territoryDenyUseageMaterialsWhenOffline.contains(material))
				return true; // Item isn't one we're preventing for offline factions.
		}

		if (otherFaction.isNone())
		{
			if (!Conf.wildernessDenyUseage || Conf.worldsNoWildernessProtection.contains(location.getWorld().getName()))
				return true; // This is not faction territory. Use whatever you like here.
			
			if (!justCheck)
				me.msg("<b>You can't use <h>%s<b> in the wilderness.", TextUtil.getMaterialName(material));

			return false;
		}
		else if (otherFaction.isSafeZone())
		{
			if (!Conf.safeZoneDenyUseage || Permission.MANAGE_SAFE_ZONE.has(player))
				return true;

			if (!justCheck)
				me.msg("<b>You can't use <h>%s<b> in a safe zone.", TextUtil.getMaterialName(material));

			return false;
		}
		else if (otherFaction.isWarZone())
		{
			if (!Conf.warZoneDenyUseage || Permission.MANAGE_WAR_ZONE.has(player))
				return true;

			if (!justCheck)
				me.msg("<b>You can't use <h>%s<b> in a war zone.", TextUtil.getMaterialName(material));

			return false;
		}

		Faction myFaction = me.getFaction();
		Relation rel = myFaction.getRelationTo(otherFaction);

		// Cancel if we are not in our own territory
		if (rel.confDenyUseage())
		{
			if (!justCheck)
				me.msg("<b>You can't use <h>%s<b> in the territory of <h>%s<b>.", TextUtil.getMaterialName(material), otherFaction.getTag(myFaction));

			return false;
		}

		// Also cancel if player doesn't have ownership rights for this claim
		if (Conf.ownedAreasEnabled && Conf.ownedAreaDenyUseage && !otherFaction.playerHasOwnershipRights(me, loc))
		{
			if (!justCheck)
				me.msg("<b>You can't use <h>%s<b> in this territory, it is owned by: %s<b>.", TextUtil.getMaterialName(material), otherFaction.getOwnerListString(loc));

			return false;
		}
		
		
		//Check Grenzgebiet
		if(rel.isEnemy()){
			if(!Board.isFactionGrenzeAt(loc)){
				me.msg("<b>Du kannst im sicheren Gebiet <h>%s<b> nicht benutzen!", TextUtil.getMaterialName(material));
				return false;
			}
		}
		
		return true;
	}

	public static boolean canPlayerUseBlock(Player player, Block block, boolean justCheck)
	{
		FPlayer me = FPlayers.i.get(player);
		if (me.isAdminBypassing())
			return true;

		Material material = block.getType();
		FLocation loc = new FLocation(block);
		Faction otherFaction = Board.getFactionAt(loc);

		// no door/chest/whatever protection in wilderness, war zones, or safe zones
		if (!otherFaction.isNormal())
			return true;

		// We only care about some material types.
		if (otherFaction.hasPlayersOnline())
		{
			if ( ! Conf.territoryProtectedMaterials.contains(material))
				return true;
		}
		else
		{
			if ( ! Conf.territoryProtectedMaterialsWhenOffline.contains(material))
				return true;
		}

		Faction myFaction = me.getFaction();
		Relation rel = myFaction.getRelationTo(otherFaction);

		// You may use any block unless it is another faction's territory...
		if (rel.isNeutral() || (rel.isEnemy() && Conf.territoryEnemyProtectMaterials) || (rel.isAlly() && Conf.territoryAllyProtectMaterials))
		{
			if (!justCheck)
				me.msg("<b>You can't %s <h>%s<b> in the territory of <h>%s<b>.", (material == Material.SOIL ? "trample" : "use"), TextUtil.getMaterialName(material), otherFaction.getTag(myFaction));

			return false;
		}

		// Also cancel if player doesn't have ownership rights for this claim
		if (Conf.ownedAreasEnabled && Conf.ownedAreaProtectMaterials && !otherFaction.playerHasOwnershipRights(me, loc))
		{
			if (!justCheck)
				me.msg("<b>You can't use <h>%s<b> in this territory, it is owned by: %s<b>.", TextUtil.getMaterialName(material), otherFaction.getOwnerListString(loc));
			
			return false;
		}
		
		
		/*if(LWCFeatures.getEnabled()){
			if(myFaction==otherFaction){
				if(me.getRole()==Role.ADMIN){
					//if(event.isCancelled()){
					//	event.setCancelled(false);
					//}
					
					LWCFeatures.get().enforceAccess(player, LWCFeatures.get().findProtection(block), block);
				}
			}
		}*/
		
		

		return true;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		FPlayer me = FPlayers.i.get(event.getPlayer());

		me.getPower();  // update power, so they won't have gained any while dead

		Location home = me.getFaction().getHome();
		if
		(
			Conf.homesEnabled
			&&
			Conf.homesTeleportToOnDeath
			&&
			home != null
			&&
			(
				Conf.homesRespawnFromNoPowerLossWorlds
				||
				! Conf.worldsNoPowerLoss.contains(event.getPlayer().getWorld().getName())
			)
		)
		{
			event.setRespawnLocation(home);
		}
	}

	// For some reason onPlayerInteract() sometimes misses bucket events depending on distance (something like 2-3 blocks away isn't detected),
	// but these separate bucket events below always fire without fail
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event)
	{
		if (event.isCancelled()) return;

		Block block = event.getBlockClicked();
		Player player = event.getPlayer();

		if ( ! playerCanUseItemHere(player, block.getLocation(), event.getBucket(), false))
		{
			event.setCancelled(true);
			return;
		}
	}
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerBucketFill(PlayerBucketFillEvent event)
	{
		if (event.isCancelled()) return;

		Block block = event.getBlockClicked();
		Player player = event.getPlayer();

		if ( ! playerCanUseItemHere(player, block.getLocation(), event.getBucket(), false))
		{
			event.setCancelled(true);
			return;
		}
	}

	public static boolean preventCommand(String fullCmd, Player player)
	{
		if ((Conf.territoryNeutralDenyCommands.isEmpty() && Conf.territoryEnemyDenyCommands.isEmpty() && Conf.permanentFactionMemberDenyCommands.isEmpty()))
			return false;

		fullCmd = fullCmd.toLowerCase();

		FPlayer me = FPlayers.i.get(player);

		String shortCmd;  // command without the slash at the beginning
		if (fullCmd.startsWith("/"))
			shortCmd = fullCmd.substring(1);
		else
		{
			shortCmd = fullCmd;
			fullCmd = "/" + fullCmd;
		}

		if
		(
			me.hasFaction()
			&&
			! me.isAdminBypassing()
			&&
			! Conf.permanentFactionMemberDenyCommands.isEmpty()
			&&
			me.getFaction().isPermanent()
			&&
			isCommandInList(fullCmd, shortCmd, Conf.permanentFactionMemberDenyCommands.iterator())
		)
		{
			me.msg("<b>You can't use the command \""+fullCmd+"\" because you are in a permanent faction.");
			return true;
		}

		if (!me.isInOthersTerritory())
		{
			return false;
		}

		Relation rel = me.getRelationToLocation();
		if (rel.isAtLeast(Relation.ALLY))
		{
			return false;
		}

		if
		(
			rel.isNeutral()
			&&
			! Conf.territoryNeutralDenyCommands.isEmpty()
			&&
			! me.isAdminBypassing()
			&&
			isCommandInList(fullCmd, shortCmd, Conf.territoryNeutralDenyCommands.iterator())
		)
		{
			me.msg("<b>You can't use the command \""+fullCmd+"\" in neutral territory.");
			return true;
		}

		if
		(
			rel.isEnemy()
			&&
			! Conf.territoryEnemyDenyCommands.isEmpty()
			&&
			! me.isAdminBypassing()
			&&
			isCommandInList(fullCmd, shortCmd, Conf.territoryEnemyDenyCommands.iterator())
		)
		{
			me.msg("<b>You can't use the command \""+fullCmd+"\" in enemy territory.");
			return true;
		}

		return false;
	}

	private static boolean isCommandInList(String fullCmd, String shortCmd, Iterator<String> iter)
	{
		String cmdCheck;
		while (iter.hasNext())
		{
			cmdCheck = iter.next();
			if (cmdCheck == null)
			{
				iter.remove();
				continue;
			}

			cmdCheck = cmdCheck.toLowerCase();
			if (fullCmd.startsWith(cmdCheck) || shortCmd.startsWith(cmdCheck))
				return true;
		}
		return false;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerKick(PlayerKickEvent event)
	{
		if (event.isCancelled()) return;

		FPlayer badGuy = FPlayers.i.get(event.getPlayer());
		if (badGuy == null)
		{
			return;
		}

		SpoutFeatures.playerDisconnect(badGuy);

		// if player was banned (not just kicked), get rid of their stored info
		if (Conf.removePlayerDataWhenBanned && event.getReason().equals("Banned by admin."))
		{
			if (badGuy.getRole() == Role.ADMIN)
				badGuy.getFaction().promoteNewLeader();

			badGuy.leave(false);
			badGuy.detach();
		}
	}
	
	/* Inventory Events */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onInventoryClose(InventoryCloseEvent event){
		InventoryView inv=event.getView();
		Player player=(Player) event.getPlayer();
		
		p.log("TEST");
		
		for(FWar fwar:FWars.i.get()){
			p.log("TEST2");
			for(InventoryView inv2:fwar.tempInvs){
				p.log("TEST4");
				if(inv2.equals(inv)){
					p.log("TEST5");
				}
			}
			if(fwar.tempInvs.contains(inv)){
				p.log("TEST3");
				fwar.removeTempInventory(inv);
				player.sendMessage(ChatColor.GREEN+"Items hinzugef�gt!");
			}
			if(fwar.tempInvsFromTarget.contains(inv)){
				p.log("TEST3");
				fwar.removeTempInventoryFromTarget(inv);
				player.sendMessage(ChatColor.GREEN+"Items hinzugef�gt!");
			}
		}
	}
	@EventHandler(priority = EventPriority.NORMAL)
	public void onInventoryClick(InventoryClickEvent event){
		InventoryView inv=event.getView();
		Player player=(Player) event.getWhoClicked();
		ItemStack istack=event.getCurrentItem();
		
		for(FPlayer fpl:FPlayers.i.get()){
			if(inv.equals(fpl.playerInventoryView)){
				event.setCancelled(true);
				
				if(event.getRawSlot()<54){
					ItemStack dataStack=istack.clone();
					
					ItemStack lostItems= player.getInventory().addItem(istack).get(0);
					event.setCurrentItem(lostItems);
					
					for(Material mat:fpl.getFaction().factionInventory.keySet()){
						Integer[] args=fpl.getFaction().factionInventory.get(mat);
						
						if(mat==dataStack.getType()){
							if(lostItems!=null){
								args[1]=args[1]-(dataStack.getAmount()-lostItems.getAmount());
							}else{
								args[1]=args[1]-dataStack.getAmount();
							}
							
							fpl.getFaction().factionInventory.put(mat, args);
						}				 
					}
				}
			}
		}
		
		
	}
}