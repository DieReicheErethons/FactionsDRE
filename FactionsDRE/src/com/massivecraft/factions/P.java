package com.massivecraft.factions;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import com.massivecraft.factions.cmd.CmdAutoHelp;
import com.massivecraft.factions.cmd.FCmdRoot;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.factions.integration.EssentialsFeatures;
import com.massivecraft.factions.integration.LWCFeatures;
import com.massivecraft.factions.integration.Worldguard;
import com.massivecraft.factions.integration.capi.CapiFeatures;
import com.massivecraft.factions.integration.spout.SpoutFeatures;
import com.massivecraft.factions.listeners.FactionsBlockListener;
import com.massivecraft.factions.listeners.FactionsChatEarlyListener;
import com.massivecraft.factions.listeners.FactionsEntityListener;
import com.massivecraft.factions.listeners.FactionsInventoryListener;
import com.massivecraft.factions.listeners.FactionsPlayerListener;
import com.massivecraft.factions.listeners.FactionsServerListener;
import com.massivecraft.factions.struct.ChatMode;
import com.massivecraft.factions.util.AutoLeaveTask;
import com.massivecraft.factions.util.MapFLocToStringSetTypeAdapter;
import com.massivecraft.factions.util.MyLocationTypeAdapter;
import com.massivecraft.factions.zcore.MPlugin;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;


public class P extends MPlugin
{
	// Our single plugin instance
	public static P p;

	// Listeners
	public final FactionsPlayerListener playerListener;
	public final FactionsInventoryListener inventoryListener;
	public final FactionsChatEarlyListener chatEarlyListener;
	public final FactionsEntityListener entityListener;
	public final FactionsBlockListener blockListener;
	public final FactionsServerListener serverListener;

	// Persistance related
	private boolean locked = false;
	public boolean getLocked() {return this.locked;}
	public void setLocked(boolean val) {this.locked = val; this.setAutoSave(val);}
	private Integer AutoLeaveTask = null;

	// Commands
	public FCmdRoot cmdBase;
	public CmdAutoHelp cmdAutoHelp;

	public P()
	{
		p = this;
		this.playerListener = new FactionsPlayerListener(this);
		this.inventoryListener = new FactionsInventoryListener(this);
		this.chatEarlyListener = new FactionsChatEarlyListener(this);
		this.entityListener = new FactionsEntityListener(this);
		this.blockListener = new FactionsBlockListener(this);
		this.serverListener = new FactionsServerListener(this);
	}


	@Override
	public void onEnable()
	{
		if ( ! preEnable()) return;
		this.loadSuccessful = false;



		// Load Conf from disk
		Conf.load();

		// Add Base Commands
		this.cmdBase = new FCmdRoot();
		this.cmdAutoHelp = new CmdAutoHelp();
		this.getBaseCommands().add(cmdBase);

		//Setup
		EssentialsFeatures.setup();
		SpoutFeatures.setup();
		Econ.setup();
		CapiFeatures.setup();
		LWCFeatures.setup();

		// Load Saved Data
		FPlayers.i.loadFromDisc();
		Factions.i.loadFromDisc();
		FWars.i.loadFromDisc();
		Board.load();


		if(Conf.worldGuardChecking)
		{
			Worldguard.init(this);
		}

		// start up task which runs the autoLeaveAfterDaysOfInactivity routine
		startAutoLeaveTask(false);

		// Register Event Handlers
		getServer().getPluginManager().registerEvents(playerListener, this);
		getServer().getPluginManager().registerEvents(inventoryListener, this);
		getServer().getPluginManager().registerEvents(chatEarlyListener, this);
		getServer().getPluginManager().registerEvents(entityListener, this);
		getServer().getPluginManager().registerEvents(blockListener, this);
		getServer().getPluginManager().registerEvents(serverListener, this);

		postEnable();
		this.loadSuccessful = true;

		// -------------------------------------------- //
		// Update Sheduler
		// -------------------------------------------- //

		//Per Second
		p.getServer().getScheduler().scheduleAsyncRepeatingTask(p, new Runnable() {
		    public void run() {
		    	FPlayers.update();
		    }
		}, 0L, 20L);

		//Per Minute
		p.getServer().getScheduler().scheduleAsyncRepeatingTask(p, new Runnable() {
		    public void run() {
		    	Board.updateNZone();

		    	FWar.checkForDeleteFWars();
		    	FWar.setRelationshipWhenTimeToWarIsOver();
		    	//Taxation Update
				for(FPlayer fplayer:FPlayers.i.getOnline()){
					fplayer.getTax();
				}


		    }
		}, 0L, 1200L);

		//Per Hour
		p.getServer().getScheduler().scheduleAsyncRepeatingTask(p, new Runnable() {
		    public void run() {
		    	Factions.i.updateBeginnersProtection();

		    	FWar.payForMorePlayersThenTarget();
		    	Faction.checkAfterWarProtections();
		    }
		}, 0L, 72000L);

	}

	@Override
	public GsonBuilder getGsonBuilder()
	{
		Type mapFLocToStringSetType = new TypeToken<Map<FLocation, Set<String>>>(){}.getType();

		return new GsonBuilder()
		.setPrettyPrinting()
		.disableHtmlEscaping()
		.excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.VOLATILE)
		.registerTypeAdapter(Location.class, new MyLocationTypeAdapter())
		.registerTypeAdapter(mapFLocToStringSetType, new MapFLocToStringSetTypeAdapter());
	}

	@Override
	public void onDisable()
	{
		// only save data if plugin actually completely loaded successfully
		if (this.loadSuccessful)
		{
			Board.save();
			Conf.save();
		}
		EssentialsFeatures.unhookChat();
		if (AutoLeaveTask != null)
		{
			this.getServer().getScheduler().cancelTask(AutoLeaveTask);
			AutoLeaveTask = null;
		}
		super.onDisable();
	}

	public void startAutoLeaveTask(boolean restartIfRunning)
	{
		if (AutoLeaveTask != null)
		{
			if ( ! restartIfRunning) return;
			this.getServer().getScheduler().cancelTask(AutoLeaveTask);
		}

		if (Conf.autoLeaveRoutineRunsEveryXMinutes > 0.0)
		{
			long ticks = (long)(20 * 60 * Conf.autoLeaveRoutineRunsEveryXMinutes);
			AutoLeaveTask = getServer().getScheduler().scheduleSyncRepeatingTask(this, new AutoLeaveTask(), ticks, ticks);
		}
	}

	@Override
	public void postAutoSave()
	{
		Board.save();
		Conf.save();
	}

	@Override
	public boolean logPlayerCommands()
	{
		return Conf.logPlayerCommands;
	}

	@Override
	public boolean handleCommand(CommandSender sender, String commandString, boolean testOnly)
	{
		if (sender instanceof Player && FactionsPlayerListener.preventCommand(commandString, (Player)sender)) return true;

		return super.handleCommand(sender, commandString, testOnly);
	}



	// -------------------------------------------- //
	// Functions for other plugins to hook into
	// -------------------------------------------- //

	// This value will be updated whenever new hooks are added
	public int hookSupportVersion()
	{
		return 3;
	}

	// If another plugin is handling insertion of chat tags, this should be used to notify Factions
	public void handleFactionTagExternally(boolean notByFactions)
	{
		Conf.chatTagHandledByAnotherPlugin = notByFactions;
	}

	// Simply put, should this chat event be left for Factions to handle? For now, that means players with Faction Chat
	// enabled or use of the Factions f command without a slash; combination of isPlayerFactionChatting() and isFactionsCommand()


	public boolean shouldLetFactionsHandleThisChat(AsyncPlayerChatEvent event)
	{
		if (event == null) return false;
		return (isPlayerFactionChatting(event.getPlayer()) || isFactionsCommand(event.getMessage()));
	}

	// Does player have Faction Chat enabled? If so, chat plugins should preferably not do channels,
	// local chat, or anything else which targets individual recipients, so Faction Chat can be done
	public boolean isPlayerFactionChatting(Player player)
	{
		if (player == null) return false;
		FPlayer me = FPlayers.i.get(player);

		if (me == null)return false;
		return me.getChatMode().isAtLeast(ChatMode.ALLIANCE);
	}

	// Is this chat message actually a Factions command, and thus should be left alone by other plugins?

	// TODO: GET THIS BACK AND WORKING

	public boolean isFactionsCommand(String check)
	{
		if (check == null || check.isEmpty()) return false;
		return this.handleCommand(null, check, true);
	}

	// Get a player's faction tag (faction name), mainly for usage by chat plugins for local/channel chat
	public String getPlayerFactionTag(Player player)
	{
		return getPlayerFactionTagRelation(player, null);
	}

	// Same as above, but with relation (enemy/neutral/ally) coloring potentially added to the tag
	public String getPlayerFactionTagRelation(Player speaker, Player listener)
	{
		String tag = "~";

		if (speaker == null)
			return tag;

		FPlayer me = FPlayers.i.get(speaker);
		if (me == null)
			return tag;

		// if listener isn't set, or config option is disabled, give back uncolored tag
		if (listener == null || !Conf.chatTagRelationColored) {
			tag = me.getChatTag().trim();
		} else {
			FPlayer you = FPlayers.i.get(listener);
			if (you == null)
				tag = me.getChatTag().trim();
			else  // everything checks out, give the colored tag
				tag = me.getChatTag(you).trim();
		}
		if (tag.isEmpty())
			tag = "~";

		return tag;
	}

	// Get a player's title within their faction, mainly for usage by chat plugins for local/channel chat
	public String getPlayerTitle(Player player)
	{
		if (player == null)
			return "";

		FPlayer me = FPlayers.i.get(player);
		if (me == null)
			return "";

		return me.getTitle().trim();
	}

	// Get a list of all faction tags (names)
	public Set<String> getFactionTags()
	{
		Set<String> tags = new HashSet<String>();
		for (Faction faction : Factions.i.get())
		{
			tags.add(faction.getTag());
		}
		return tags;
	}

	// Get a list of all players in the specified faction
	public Set<String> getPlayersInFaction(String factionTag)
	{
		Set<String> players = new HashSet<String>();
		Faction faction = Factions.i.getByTag(factionTag);
		if (faction != null)
		{
			for (FPlayer fplayer : faction.getFPlayers())
			{
				players.add(fplayer.getName());
			}
		}
		return players;
	}

	// Get a list of all online players in the specified faction
	public Set<String> getOnlinePlayersInFaction(String factionTag)
	{
		Set<String> players = new HashSet<String>();
		Faction faction = Factions.i.getByTag(factionTag);
		if (faction != null)
		{
			for (FPlayer fplayer : faction.getFPlayersWhereOnline(true))
			{
				players.add(fplayer.getName());
			}
		}
		return players;
	}

	// check if player is allowed to build/destroy in a particular location
	public boolean isPlayerAllowedToBuildHere(Player player, Location location)
	{
		return FactionsBlockListener.playerCanBuildDestroyBlock(player, player.getWorld().getBlockAt(location), "", true);
	}

	// check if player is allowed to interact with the specified block (doors/chests/whatever)
	public boolean isPlayerAllowedToInteractWith(Player player, Block block)
	{
		return FactionsPlayerListener.canPlayerUseBlock(player, block, true);
	}

	// check if player is allowed to use a specified item (flint&steel, buckets, etc) in a particular location
	public boolean isPlayerAllowedToUseThisHere(Player player, Location location, Material material)
	{
		return FactionsPlayerListener.playerCanUseItemHere(player, location, material, true);
	}

	// execute change Faction commands
	public void executeFactionChangeCommands(Set<String> commands, Player player){
		for(String command:commands){
			command = command.replace("%player%", player.getName());
			command = command.replace("%faction%", FPlayers.i.get(player).getFaction().getTag());
			String[] splittedCommand=command.split(";");
			if(splittedCommand.length>1){
				if(splittedCommand[0].equalsIgnoreCase("p")){
					p.getServer().dispatchCommand(player,splittedCommand[1]);
				}else{
					p.getServer().dispatchCommand(Bukkit.getConsoleSender(),splittedCommand[1]);
				}
			}else{
				p.getServer().dispatchCommand(Bukkit.getConsoleSender(),splittedCommand[0]);
			}
		}
	}
}
