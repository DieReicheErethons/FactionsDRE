package com.massivecraft.factions;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.getspout.spoutapi.SpoutManager;
import com.massivecraft.factions.buildings.BuildingType;
import com.massivecraft.factions.iface.EconomyParticipator;
import com.massivecraft.factions.iface.RelationParticipator;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.factions.integration.LWCFeatures;
import com.massivecraft.factions.integration.Worldguard;
import com.massivecraft.factions.integration.spout.SpoutFeatures;
import com.massivecraft.factions.integration.spout.SpoutMenu;
import com.massivecraft.factions.struct.ChatMode;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.util.RelationUtil;
import com.massivecraft.factions.zcore.persist.PlayerEntity;

/**
 * Logged in players always have exactly one FPlayer instance. Logged out
 * players may or may not have an FPlayer instance. They will always have one if
 * they are part of a faction. This is because only players with a faction are
 * saved to disk (in order to not waste disk space).
 * 
 * The FPlayer is linked to a minecraft player using the player name.
 * 
 * The same instance is always returned for the same player. This means you can
 * use the == operator. No .equals method necessary.
 */

public class FPlayer extends PlayerEntity implements EconomyParticipator {
	// private transient String playerName;
	private transient FLocation lastStoodAt = new FLocation(); // Where did this
																// player stand
																// the last time
																// we checked?

	// FIELD: factionId
	private String factionId;

	public Faction getFaction() {
		if (this.factionId == null) {
			return null;
		}
		return Factions.i.get(this.factionId);
	}

	public String getFactionId() {
		return this.factionId;
	}

	public boolean hasFaction() {
		return !factionId.equals("0");
	}

	public void setFaction(Faction faction) {
		Faction oldFaction = this.getFaction();
		if (oldFaction != null)
			oldFaction.removeFPlayer(this);
		faction.addFPlayer(this);
		this.factionId = faction.getId();
		SpoutFeatures.updateAppearances(this.getPlayer());

	}

	// FIELD: role
	private Role role;

	public Role getRole() {
		return this.role;
	}

	public void setRole(Role role) {
		this.role = role;
		SpoutFeatures.updateAppearances(this.getPlayer());
	}

	// FIELD: title
	private String title;

	// FIELD: power
	private double power;

	// FIELD: power
	private double lastpower;

	// FIELD: powerBoost
	// special increase/decrease to min and max power for this player
	private double powerBoost;

	public double getPowerBoost() {
		return this.powerBoost;
	}

	public void setPowerBoost(double powerBoost) {
		this.powerBoost = powerBoost;
	}

	// FIELD: lastPowerUpdateTime
	private long lastPowerUpdateTime;

	// FIELD: lastLoginTime
	private long lastLoginTime;

	// FIELD: mapAutoUpdating
	private transient boolean mapAutoUpdating;

	// FIELD: autoClaimEnabled
	private transient Faction autoClaimFor;

	// FIELD: InventoryView
	public transient InventoryView playerInventoryView;

	public Faction getAutoClaimFor() {
		return autoClaimFor;
	}

	public void setAutoClaimFor(Faction faction) {
		this.autoClaimFor = faction;
		if (this.autoClaimFor != null) {
			// TODO: merge these into same autoclaim
			this.autoSafeZoneEnabled = false;
			this.autoWarZoneEnabled = false;
		}
	}

	// FIELD: autoSafeZoneEnabled
	private transient boolean autoSafeZoneEnabled;

	public boolean isAutoSafeClaimEnabled() {
		return autoSafeZoneEnabled;
	}

	public void setIsAutoSafeClaimEnabled(boolean enabled) {
		this.autoSafeZoneEnabled = enabled;
		if (enabled) {
			this.autoClaimFor = null;
			this.autoWarZoneEnabled = false;
		}
	}

	// FIELD: autoWarZoneEnabled
	private transient boolean autoWarZoneEnabled;

	public boolean isAutoWarClaimEnabled() {
		return autoWarZoneEnabled;
	}

	public void setIsAutoWarClaimEnabled(boolean enabled) {
		this.autoWarZoneEnabled = enabled;
		if (enabled) {
			this.autoClaimFor = null;
			this.autoSafeZoneEnabled = false;
		}
	}

	private boolean isAdminBypassing = false;

	public boolean isAdminBypassing() {
		return this.isAdminBypassing;
	}

	public void setIsAdminBypassing(boolean val) {
		this.isAdminBypassing = val;
	}

	// FIELD: loginPvpDisabled
	private transient boolean loginPvpDisabled;

	// FIELD: deleteMe
	private transient boolean deleteMe;

	// FIELD: chatMode
	private ChatMode chatMode;

	public void setChatMode(ChatMode chatMode) {
		this.chatMode = chatMode;
	}

	public ChatMode getChatMode() {
		if (this.factionId.equals("0") || !Conf.factionOnlyChat) {
			this.chatMode = ChatMode.PUBLIC;
		}
		return chatMode;
	}

	// FIELD: chatSpy
	private boolean spyingChat = false;

	public void setSpyingChat(boolean chatSpying) {
		this.spyingChat = chatSpying;
	}

	public boolean isSpyingChat() {
		return spyingChat;
	}

	// FIELD: account
	public String getAccountId() {
		return this.getId();
	}

	// FIELD: taxation by Frank
	public double tax;
	public long taxoldtime;
	public long taxtime;

	public double getTax() {
		this.updateTax();
		return this.tax;
	}

	private void updateTax() {

		if (this.hasFaction()) {
			long hour = 1000 * 60 * 60;

			long current = System.currentTimeMillis();

			if (this.taxoldtime == 0) {
				this.taxoldtime = current;
			}

			long timedifferent = current - taxoldtime;

			this.taxtime = this.taxtime + timedifferent;

			if (this.taxtime > hour) {
				this.tax = this.tax + getFaction().getTax();
				this.msg("<i>The hourly taxes (<b>%s<i>) were added. Write '/ f tax pay' to pay your debts. Your debts are <b>%s", getFaction().getTax(), tax);

				this.taxtime = this.taxtime - hour;
			}

			this.taxoldtime = current;
		}
	}

	public void payTax(double ammount) {
		if (ammount == 0) {
			if (Econ.transferMoney(this, this, this.getFaction(), tax)) {
				tax = 0;
			}
		} else {
			if (Econ.transferMoney(this, this, this.getFaction(), ammount)) {
				tax = tax - ammount;
			}
		}
	}

	// FIELD: Spoutfeatures by Frank
	private transient SpoutMenu sMenu;

	public void initSpoutMenu() {
		if (SpoutFeatures.enabled()) {
			setsMenu(new SpoutMenu(this, SpoutManager.getPlayer(this.getPlayer())));
		}
	}

	// FIELD: Building by Frank
	private transient BuildingType isBuilding;

	private transient Set<Block> changedBlock = new HashSet<Block>();
	private transient Location lastseelocation;
	private transient long buildingticker = 0;

	// Claim by Frank
	private transient String isclaimabuilding = null;
	private transient Block firstclaimblock = null;
	private transient Block secondclaimblock = null;

	// Getter and Setter by Frank
	public Set<Block> getChangedBlock() {
		return changedBlock;
	}

	public void setChangedBlock(Set<Block> changedBlock) {
		this.changedBlock = changedBlock;
	}

	public String getIsclaimabuilding() {
		return isclaimabuilding;
	}

	public void setIsclaimabuilding(String isclaimabuilding) {
		this.isclaimabuilding = isclaimabuilding;
	}

	public Block getFirstclaimblock() {
		return firstclaimblock;
	}

	public void setFirstclaimblock(Block firstclaimblock) {
		this.firstclaimblock = firstclaimblock;
	}

	public Block getSecondclaimblock() {
		return secondclaimblock;
	}

	public void setSecondclaimblock(Block secondclaimblock) {
		this.secondclaimblock = secondclaimblock;
	}

	public BuildingType isBuilding() {
		return isBuilding;
	}

	public void setIsBuilding(BuildingType isBuilding) {
		this.isBuilding = isBuilding;
	}

	public Location getLastseelocation() {
		return lastseelocation;
	}

	public void setLastseelocation(Location lastseelocation) {
		this.lastseelocation = lastseelocation;
	}

	public SpoutMenu getsMenu() {
		return sMenu;
	}

	public void setsMenu(SpoutMenu sMenu) {
		this.sMenu = sMenu;
	}

	public long getBuildingticker() {
		return buildingticker;
	}

	public void setBuildingticker(long buildingticker) {
		this.buildingticker = buildingticker;
	}

	// -------------------------------------------- //
	// Construct
	// -------------------------------------------- //

	// GSON need this noarg constructor.
	public FPlayer() {
		this.resetFactionData(false);
		this.setLastpower(0);
		this.power = Conf.powerPlayerStarting;
		this.lastPowerUpdateTime = System.currentTimeMillis();
		this.lastLoginTime = System.currentTimeMillis();
		this.mapAutoUpdating = false;
		this.autoClaimFor = null;
		this.autoSafeZoneEnabled = false;
		this.autoWarZoneEnabled = false;
		this.loginPvpDisabled = (Conf.noPVPDamageToOthersForXSecondsAfterLogin > 0) ? true : false;
		this.deleteMe = false;
		this.powerBoost = 0.0;

		if (!Conf.newPlayerStartingFactionID.equals("0") && Factions.i.exists(Conf.newPlayerStartingFactionID)) {
			this.factionId = Conf.newPlayerStartingFactionID;
		}
	}

	public final void resetFactionData(boolean doSpoutUpdate) {
		// clean up any territory ownership in old faction, if there is one
		if (Factions.i.exists(this.getFactionId())) {
			Faction currentFaction = this.getFaction();
			currentFaction.removeFPlayer(this);
			if (currentFaction.isNormal()) {
				currentFaction.clearClaimOwnership(this.getId());
			}
		}

		this.factionId = "0"; // The default neutral faction
		this.chatMode = ChatMode.PUBLIC;
		this.role = Role.NORMAL;
		this.title = "";
		this.autoClaimFor = null;

		// Taxation by Frank
		this.tax = 0;
		this.taxoldtime = 0;
		this.taxtime = 0;
		this.getTax();

		if (doSpoutUpdate) {
			SpoutFeatures.updateAppearances(this.getPlayer());
		}
	}

	public void resetFactionData() {
		this.resetFactionData(true);
	}

	// -------------------------------------------- //
	// Getters And Setters
	// -------------------------------------------- //

	public long getLastLoginTime() {
		return lastLoginTime;
	}

	public void setLastLoginTime(long lastLoginTime) {
		losePowerFromBeingOffline();
		this.lastLoginTime = lastLoginTime;
		this.lastPowerUpdateTime = lastLoginTime;
		if (Conf.noPVPDamageToOthersForXSecondsAfterLogin > 0) {
			this.loginPvpDisabled = true;
		}
	}

	public boolean isMapAutoUpdating() {
		return mapAutoUpdating;
	}

	public void setMapAutoUpdating(boolean mapAutoUpdating) {
		this.mapAutoUpdating = mapAutoUpdating;
	}

	public boolean hasLoginPvpDisabled() {
		if (!loginPvpDisabled) {
			return false;
		}
		if (this.lastLoginTime + (Conf.noPVPDamageToOthersForXSecondsAfterLogin * 1000) < System.currentTimeMillis()) {
			this.loginPvpDisabled = false;
			return false;
		}
		return true;
	}

	public FLocation getLastStoodAt() {
		return this.lastStoodAt;
	}

	public void setLastStoodAt(FLocation flocation) {
		this.lastStoodAt = flocation;
	}

	public void markForDeletion(boolean delete) {
		deleteMe = delete;
	}

	// ----------------------------------------------//
	// Title, Name, Faction Tag and Chat
	// ----------------------------------------------//

	// Base:

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getName() {
		return this.getId(); // TODO: ... display name or remove completeley
	}

	public String getTag() {
		if (!this.hasFaction()) {
			return "";
		}
		return this.getFaction().getTag();
	}

	// Base concatenations:

	public String getNameAndSomething(String something) {
		String ret = this.role.getPrefix();
		if (something.length() > 0) {
			ret += something + " ";
		}
		ret += this.getName();
		return ret;
	}

	public String getNameAndTitle() {
		return this.getNameAndSomething(this.getTitle());
	}

	public String getNameAndTag() {
		return this.getNameAndSomething(this.getTag());
	}

	// Colored concatenations:
	// These are used in information messages

	public String getNameAndTitle(Faction faction) {
		return this.getColorTo(faction) + this.getNameAndTitle();
	}

	public String getNameAndTitle(FPlayer fplayer) {
		return this.getColorTo(fplayer) + this.getNameAndTitle();
	}

	/*
	 * public String getNameAndTag(Faction faction) { return
	 * this.getRelationColor(faction)+this.getNameAndTag(); } public String
	 * getNameAndTag(FPlayer fplayer) { return
	 * this.getRelationColor(fplayer)+this.getNameAndTag(); }
	 */

	// TODO: REmovded for refactoring.

	/*
	 * public String getNameAndRelevant(Faction faction) { // Which relation?
	 * Relation rel = this.getRelationTo(faction);
	 * 
	 * // For member we show title if (rel == Relation.MEMBER) { return
	 * rel.getColor() + this.getNameAndTitle(); }
	 * 
	 * // For non members we show tag return rel.getColor() +
	 * this.getNameAndTag(); } public String getNameAndRelevant(FPlayer fplayer)
	 * { return getNameAndRelevant(fplayer.getFaction()); }
	 */

	// Chat Tag:
	// These are injected into the format of global chat messages.

	public String getChatTag() {
		if (!this.hasFaction()) {
			return "";
		}

		return String.format(Conf.chatTagFormat, this.role.getPrefix() + this.getTag());
	}

	// Colored Chat Tag
	public String getChatTag(Faction faction) {
		if (!this.hasFaction()) {
			return "";
		}

		return this.getRelationTo(faction).getColor() + getChatTag();
	}

	public String getChatTag(FPlayer fplayer) {
		if (!this.hasFaction()) {
			return "";
		}

		return this.getColorTo(fplayer) + getChatTag();
	}

	// -------------------------------
	// Relation and relation colors
	// -------------------------------

	@Override
	public String describeTo(RelationParticipator that, boolean ucfirst) {
		return RelationUtil.describeThatToMe(this, that, ucfirst);
	}

	@Override
	public String describeTo(RelationParticipator that) {
		return RelationUtil.describeThatToMe(this, that);
	}

	@Override
	public Relation getRelationTo(RelationParticipator rp) {
		return RelationUtil.getRelationTo(this, rp);
	}

	@Override
	public Relation getRelationTo(RelationParticipator rp, boolean ignorePeaceful) {
		return RelationUtil.getRelationTo(this, rp, ignorePeaceful);
	}

	public Relation getRelationToLocation() {
		return Board.getFactionAt(new FLocation(this)).getRelationTo(this);
	}

	@Override
	public ChatColor getColorTo(RelationParticipator rp) {
		return RelationUtil.getColorOfThatToMe(this, rp);
	}

	// ----------------------------------------------//
	// Health
	// ----------------------------------------------//
	public void heal(int amnt) {
		Player player = this.getPlayer();
		if (player == null) {
			return;
		}
		player.setHealth(player.getHealth() + amnt);
	}

	// ----------------------------------------------//
	// Power
	// ----------------------------------------------//
	public double getPower() {
		this.updatePower();
		return this.power;
	}

	public double getLastpower() {
		return lastpower;
	}

	public void setLastpower(double lastpower) {
		this.lastpower = lastpower;
	}

	public void setPower(double newpower) {
		this.power = newpower;
	}

	public void alterPower(double delta) {
		this.power += delta;
		if (this.power > this.getPowerMax())
			this.power = this.getPowerMax();
		else if (this.power < this.getPowerMin())
			this.power = this.getPowerMin();
	}

	public double getPowerMax() {
		return Conf.powerPlayerMax + this.powerBoost;
	}

	public double getPowerMin() {
		return Conf.powerPlayerMin + this.powerBoost;
	}

	public int getPowerRounded() {
		return (int) Math.round(this.getPower());
	}

	public int getPowerMaxRounded() {
		return (int) Math.round(this.getPowerMax());
	}

	public int getPowerMinRounded() {
		return (int) Math.round(this.getPowerMin());
	}

	protected void updatePower() {
		if (this.isOffline()) {
			losePowerFromBeingOffline();
			if (!Conf.powerRegenOffline) {
				return;
			}
		}
		long now = System.currentTimeMillis();
		long millisPassed = now - this.lastPowerUpdateTime;
		this.lastPowerUpdateTime = now;

		Player thisPlayer = this.getPlayer();
		if (thisPlayer != null && thisPlayer.isDead())
			return; // don't let dead players regain power until they respawn

		int millisPerMinute = 60 * 1000;
		if (!Conf.fwarDisablePowerRegen) {
			this.alterPower(millisPassed * Conf.powerPerMinute / millisPerMinute);
		} else {
			if (!FWar.isFactionInWar(this.getFaction())) {
				this.alterPower(millisPassed * Conf.powerPerMinute / millisPerMinute);
			}
		}
	}

	protected void losePowerFromBeingOffline() {
		if (Conf.powerOfflineLossPerDay > 0.0 && this.power > Conf.powerOfflineLossLimit) {
			if((!this.getFaction().isInWar()) && (Conf.fwarOnlyPowerLossPerDayInWar)){
				return;
			}
			long now = System.currentTimeMillis();
			if(Conf.hoursBeforePowerOfflineLossPerDayIsActive > 0.0){
				if(now < lastLoginTime+(Conf.hoursBeforePowerOfflineLossPerDayIsActive * 60 * 60 * 1000))
					return;
			}
			
			long millisPassed = now - this.lastPowerUpdateTime;
			this.lastPowerUpdateTime = now;
	
			double loss = millisPassed * Conf.powerOfflineLossPerDay / (24 * 60 * 60 * 1000);
			if (this.power - loss < Conf.powerOfflineLossLimit) {
				loss = this.power;
			}
			this.alterPower(-loss);
		}
	}

	public void onDeath() {
		this.updatePower();
		this.alterPower(-Conf.powerPerDeath);
	}

	// ----------------------------------------------//
	// Territory
	// ----------------------------------------------//
	public boolean isInOwnTerritory() {
		return Board.getFactionAt(new FLocation(this)) == this.getFaction();
	}

	public boolean isInOthersTerritory() {
		Faction factionHere = Board.getFactionAt(new FLocation(this));
		return factionHere != null && factionHere.isNormal() && factionHere != this.getFaction();
	}

	public boolean isInAllyTerritory() {
		return Board.getFactionAt(new FLocation(this)).getRelationTo(this).isAlly();
	}

	public boolean isInNeutralTerritory() {
		return Board.getFactionAt(new FLocation(this)).getRelationTo(this).isNeutral();
	}

	public boolean isInEnemyTerritory() {
		return Board.getFactionAt(new FLocation(this)).getRelationTo(this).isEnemy();
	}

	public void sendFactionHereMessage() {
		if (SpoutFeatures.updateTerritoryDisplay(this)) {
			return;
		}
		Faction factionHere = Board.getFactionAt(new FLocation(this));
		String msg = P.p.txt.parse("<i>") + " ~ " + factionHere.getTag(this);
		if (factionHere.getDescription().length() > 0) {
			msg += " - " + factionHere.getDescription();
		}
		this.sendMessage(msg);
	}

	// -------------------------------
	// Actions
	// -------------------------------
	public void join(Faction faction) {
		if (!faction.isNormal()) {
			msg("<b>You may only join normal factions. This is a system faction.");
			return;
		}

		if (faction == this.getFaction()) {
			msg("<b>You are already a member of %s", faction.getTag(this));
			return;
		}

		if (this.hasFaction()) {
			msg("<b>You must leave your current faction first.");
			return;
		}

		if (!Conf.canLeaveWithNegativePower && this.getPower() < 0) {
			msg("<b>You cannot join a faction until your power is positive.");
			return;
		}

		if (!(faction.getOpen() || faction.isInvited(this) || this.isAdminBypassing())) {
			msg("<i>This faction requires invitation.");
			faction.msg("%s<i> tried to join your faction.", this.describeTo(faction, true));
			return;
		}

		// if economy is enabled, they're not on the bypass list, and this
		// command has a cost set, make 'em pay
		if (!payForCommand(Conf.econCostJoin, "to join a faction", "for joining a faction"))
			return;

		this.msg("<i>You successfully joined %s", faction.getTag(this));
		faction.msg("<i>%s joined your faction.", this.describeTo(faction, true));

		this.resetFactionData();
		this.setFaction(faction);
		faction.deinvite(this);

		if (Conf.logFactionJoin)
			P.p.log(this.getName() + " joined the faction: " + faction.getTag());
	}

	public boolean payForCommand(double cost, String toDoThis, String forDoingThis) {
		if (!Econ.shouldBeUsed() || this == null || cost == 0.0 || this.isAdminBypassing())
			return true;

		if (Conf.bankEnabled && Conf.bankFactionPaysCosts && this.hasFaction()) {
			if (!Econ.modifyMoney(this.getFaction(), -cost, toDoThis, forDoingThis))
				return false;
		} else {
			if (!Econ.modifyMoney(this, -cost, toDoThis, forDoingThis))
				return false;
		}
		return true;
	}

	public void leave(boolean makePay) {
		Faction myFaction = this.getFaction();

		if (myFaction == null) {
			resetFactionData();
			return;
		}

		boolean perm = myFaction.isPermanent();

		if (!perm && this.getRole() == Role.ADMIN && myFaction.getFPlayers().size() > 1) {
			msg("<b>You must give the admin role to someone else first.");
			return;
		}

		if (!Conf.canLeaveWithNegativePower && this.getPower() < 0) {
			msg("<b>You cannot leave until your power is positive.");
			return;
		}

		// if economy is enabled and they're not on the bypass list, make 'em
		// pay
		if (makePay && Econ.shouldBeUsed() && !this.isAdminBypassing()) {
			double cost = Conf.econCostLeave;
			if (!Econ.modifyMoney(this, -cost, "to leave your faction.", "for leaving your faction."))
				return;
		}

		// Am I the last one in the faction?
		if (myFaction.getFPlayers().size() == 1) {
			// Transfer all money
			if (Econ.shouldBeUsed())
				Econ.transferMoney(this, myFaction, this, Econ.getBalance(myFaction.getAccountId()));
		}

		if (myFaction.isNormal()) {
			for (FPlayer fplayer : myFaction.getFPlayersWhereOnline(true)) {
				fplayer.msg("%s<i> left %s<i>.", this.describeTo(fplayer, true), myFaction.describeTo(fplayer));
			}

			if (Conf.logFactionLeave)
				P.p.log(this.getName() + " left the faction: " + myFaction.getTag());
		}

		this.resetFactionData();

		if (this.getPower() > 0) {
			this.setPower(0);
		}

		if (this.getPower() < 0) {
			this.setLastpower(this.getPower());
		}

		if (myFaction.isNormal() && !perm && myFaction.getFPlayers().isEmpty()) {
			// Remove this faction
			for (FPlayer fplayer : FPlayers.i.getOnline()) {
				fplayer.msg("<i>%s<i> was disbanded.", myFaction.describeTo(fplayer, true));
			}

			myFaction.detach();
			if (Conf.logFactionDisband)
				P.p.log("The faction " + myFaction.getTag() + " (" + myFaction.getId() + ") was disbanded due to the last player (" + this.getName() + ") leaving.");

			// Remove all wars
			FWar.removeFactionWars(myFaction);
		}
	}

	public boolean canClaimForFaction(Faction forFaction) {
		if (forFaction.isNone())
			return false;

		if (this.isAdminBypassing() || (forFaction == this.getFaction() && this.getRole().isAtLeast(Role.MODERATOR)) || (forFaction.isSafeZone() && Permission.MANAGE_SAFE_ZONE.has(getPlayer()))
				|| (forFaction.isWarZone() && Permission.MANAGE_WAR_ZONE.has(getPlayer()))) {
			return true;
		}

		return false;
	}

	public boolean canClaimForFactionAtLocation(Faction forFaction, Location location, boolean notifyFailure) {
		String error = null;
		FLocation flocation = new FLocation(location);
		Faction myFaction = getFaction();
		Faction currentFaction = Board.getFactionAt(flocation);
		int ownedLand = forFaction.getLandRounded();

		if (Conf.worldGuardChecking && Worldguard.checkForRegionsInChunk(location)) {
			// Checks for WorldGuard regions in the chunk attempting to be
			// claimed
			error = P.p.txt.parse("<b>This land is protected");
		} else if (Conf.worldsNoClaiming.contains(flocation.getWorldName())) {
			error = P.p.txt.parse("<b>Sorry, this world has land claiming disabled.");
		} else if (this.isAdminBypassing()) {
			return true;
		} else if (forFaction.isSafeZone() && Permission.MANAGE_SAFE_ZONE.has(getPlayer())) {
			return true;
		} else if (forFaction.isWarZone() && Permission.MANAGE_WAR_ZONE.has(getPlayer())) {
			return true;
		} else if (myFaction != forFaction) {
			error = P.p.txt.parse("<b>You can't claim land for <h>%s<b>.", forFaction.describeTo(this));
		} else if (forFaction == currentFaction) {
			error = P.p.txt.parse("%s<i> already own this land.", forFaction.describeTo(this, true));
		} else if (this.getRole().value < Role.MODERATOR.value) {
			error = P.p.txt.parse("<b>You must be <h>%s<b> to claim land.", Role.MODERATOR.toString());
		} else if (forFaction.getFPlayers().size() < Conf.claimsRequireMinFactionMembers) {
			error = P.p.txt.parse("Factions must have at least <h>%s<b> members to claim land.", Conf.claimsRequireMinFactionMembers);
		} else if (currentFaction.isSafeZone()) {
			error = P.p.txt.parse("<b>You can not claim a Safe Zone.");
		} else if (currentFaction.isWarZone()) {
			error = P.p.txt.parse("<b>You can not claim a War Zone.");
		} else if (ownedLand >= forFaction.getPowerRounded()) {
			error = P.p.txt.parse("<b>You can't claim more land! You need more power!");
		} else if (Conf.claimedLandsMax != 0 && ownedLand >= Conf.claimedLandsMax && forFaction.isNormal()) {
			error = P.p.txt.parse("<b>Limit reached. You can't claim more land!");
		} else if (currentFaction.getRelationTo(forFaction) == Relation.ALLY) {
			error = P.p.txt.parse("<b>You can't claim the land of your allies.");
		} else if (Conf.claimsMustBeConnected && !this.isAdminBypassing() && myFaction.getLandRoundedInWorld(flocation.getWorldName()) > 0 && !Board.isConnectedLocation(flocation, myFaction)
				&& (!Conf.claimsCanBeUnconnectedIfOwnedByOtherFaction || !currentFaction.isNormal())) {
			if (Conf.claimsCanBeUnconnectedIfOwnedByOtherFaction)
				error = P.p.txt.parse("<b>You can only claim additional land which is connected to your first claim or controlled by another faction!");
			else
				error = P.p.txt.parse("<b>You can only claim additional land which is connected to your first claim!");
		} else if (currentFaction.isNormal()) {
			if (myFaction.isPeaceful()) {
				error = P.p.txt.parse("%s<i> owns this land. Your faction is peaceful, so you cannot claim land from other factions.", currentFaction.getTag(this));
			} else if (currentFaction.isPeaceful()) {
				error = P.p.txt.parse("%s<i> owns this land, and is a peaceful faction. You cannot claim land from them.", currentFaction.getTag(this));
			} else if (!currentFaction.hasLandInflation()) {
				// TODO more messages WARN current faction most importantly
				error = P.p.txt.parse("%s<i> owns this land and is strong enough to keep it.", currentFaction.getTag(this));
			} else if (!Board.isBorderLocation(flocation)) {
				error = P.p.txt.parse("<b>You must start claiming land at the border of the territory.");
			}
		}

		if (notifyFailure && error != null) {
			msg(error);
		}
		return error == null;
	}

	public boolean attemptClaim(Faction forFaction, Location location, boolean notifyFailure) {
		// notifyFailure is false if called by auto-claim; no need to notify on
		// every failure for it
		// return value is false on failure, true on success

		FLocation flocation = new FLocation(location);
		Faction currentFaction = Board.getFactionAt(flocation);

		// If not NeutralZone
		if (!currentFaction.getId().equals("-3")) {
			int ownedLand = forFaction.getLandRounded();

			if (!this.canClaimForFactionAtLocation(forFaction, location, notifyFailure))
				return false;

			// if economy is enabled and they're not on the bypass list, make
			// 'em pay
			if (Econ.shouldBeUsed() && !this.isAdminBypassing() && !forFaction.isSafeZone() && !forFaction.isWarZone()) {
				double cost = Econ.calculateClaimCost(ownedLand, currentFaction.isNormal());

				if (Conf.econClaimUnconnectedFee != 0.0 && forFaction.getLandRoundedInWorld(flocation.getWorldName()) > 0 && !Board.isConnectedLocation(flocation, this.getFaction()))
					cost += Conf.econClaimUnconnectedFee;

				if (Conf.bankEnabled && Conf.bankFactionPaysLandCosts && this.hasFaction()) {
					Faction faction = this.getFaction();
					if (!Econ.modifyMoney(faction, -cost, "to claim this land", "for claiming this land"))
						return false;

				} else {
					if (!Econ.modifyMoney(this, -cost, "to claim this land", "for claiming this land"))
						return false;

				}
			}
		}

		if (LWCFeatures.getEnabled() && forFaction.isNormal() && Conf.onCaptureResetLwcLocks) {
			LWCFeatures.clearOtherChests(flocation, this.getFaction());
		}

		// announce success
		Set<FPlayer> informTheseFPlayers = new HashSet<FPlayer>();
		informTheseFPlayers.add(this);
		informTheseFPlayers.addAll(forFaction.getFPlayersWhereOnline(true));
		for (FPlayer fp : informTheseFPlayers) {
			fp.msg("<h>%s<i> claimed land for <h>%s<i> from <h>%s<i>.", this.describeTo(fp, true), forFaction.describeTo(fp), currentFaction.describeTo(fp));
		}

		Board.setFactionAt(forFaction, flocation);
		SpoutFeatures.updateTerritoryDisplayLoc(flocation);

		if (Conf.logLandClaims)
			P.p.log(this.getName() + " claimed land at (" + flocation.getCoordString() + ") for the faction: " + forFaction.getTag());

		return true;
	}

	// -------------------------------------------- //
	// Persistance
	// -------------------------------------------- //

	@Override
	public boolean shouldBeSaved() {
		if (!this.hasFaction() && (this.getPowerRounded() == this.getPowerMaxRounded() || this.getPowerRounded() == (int) Math.round(Conf.powerPlayerStarting)))
			return false;
		return !this.deleteMe;
	}

	public void msg(String str, Object... args) {
		this.sendMessage(P.p.txt.parse(str, args));
	}

}