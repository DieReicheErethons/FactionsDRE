package com.massivecraft.factions;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.gson.reflect.TypeToken;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.zcore.persist.PlayerEntityCollection;

public class FPlayers extends PlayerEntityCollection<FPlayer> {
	public static FPlayers i = new FPlayers();

	P p = P.p;

	private FPlayers() {
		super(FPlayer.class, new CopyOnWriteArrayList<FPlayer>(), new ConcurrentSkipListMap<String, FPlayer>(String.CASE_INSENSITIVE_ORDER), new File(P.p.getDataFolder(), "players.json"), P.p.gson);

		this.setCreative(true);
	}

	@Override
	public Type getMapType() {
		return new TypeToken<Map<String, FPlayer>>() {
		}.getType();
	}

	public void clean() {
		for (FPlayer fplayer : this.get()) {
			if (!Factions.i.exists(fplayer.getFactionId())) {
				p.log("Reset faction data (invalid faction) for player " + fplayer.getName());
				fplayer.resetFactionData(false);
			}
		}
	}

	public void autoLeaveOnInactivityRoutine() {
		if (Conf.autoLeaveAfterDaysOfInactivity <= 0.0) {
			return;
		}

		long now = System.currentTimeMillis();
		double toleranceMillis = Conf.autoLeaveAfterDaysOfInactivity * 24 * 60 * 60 * 1000;

		for (FPlayer fplayer : FPlayers.i.get()) {
			if (fplayer.isOffline() && now - fplayer.getLastLoginTime() > toleranceMillis) {
				if (Conf.logFactionLeave || Conf.logFactionKick)
					P.p.log("Player " + fplayer.getName() + " was auto-removed due to inactivity.");

				// if player is faction admin, sort out the faction since he's
				// going away
				if (fplayer.getRole() == Role.ADMIN) {
					Faction faction = fplayer.getFaction();
					if (faction != null)
						fplayer.getFaction().promoteNewLeader();
				}

				fplayer.leave(false);
				fplayer.detach();
			}
		}
	}

	// -------------------------------------------- //
	// Update
	// -------------------------------------------- //

	public static void update() {
		Set<Faction> increasedFactions = new HashSet<Faction>();
		for (FPlayer me : FPlayers.i.getOnline()) {
			FLocation loc = me.getLastStoodAt();

			// If Enemy Faction
			if (Board.isFactionGrenzeAt(loc)) {
				Faction faction = Board.getFactionAt(loc);
				if (!increasedFactions.contains(faction)) {
					if (me.getRelationToLocation().isEnemy()) {
						if (faction.isNormal()) {
							if (faction.getPower() < faction.getLandRounded()) {
								increasedFactions.add(faction);

								boolean isOk = true;
								for (FPlayer enemy : faction.getFPlayersWhereOnline(true)) {
									if (enemy.getLastStoodAt().getDistanceTo(loc) < 1) {
										isOk = false;
									}
								}

								if (faction.getFPlayersWhereOnline(true).size() != 0) {
									if (isOk) {
										int actualPower = Board.incrasePower(loc, -1);

										if (actualPower % 5 == 0) {
											String sendString = "<g>Erobern [<b>";
											for (int x = 0; x < 60; x++) {
												int length = Conf.warConquestTime / 60 * x;
												if (length >= actualPower && length < actualPower + 2) {
													sendString = sendString + "<n>";
												}
												sendString = sendString + "|";
											}
											sendString = sendString + "<g>]  " + actualPower;

											for (FPlayer sendPlayer : FPlayers.i.get()) {
												if (sendPlayer.getLastStoodAt().getDistanceTo(loc) < 1) {
													sendPlayer.msg(sendString);
												}
											}
										}
									}
								}
							}
						}
					}

					// If Own Faction
					else if (me.getRelationToLocation().isMember() || me.getRelationToLocation().isAlly()) {
						if (Board.incrasePower(loc, 0) != Conf.warConquestTime) {
							boolean isOk = true;
							for (Faction checkFaction : Factions.i.get()) {
								if (checkFaction.getRelationWish(faction).isEnemy()) {
									for (FPlayer enemy : checkFaction.getFPlayers()) {
										if (enemy.getLastStoodAt().getDistanceTo(loc) < 1) {
											isOk = false;
										}
									}
								}
							}

							if (isOk) {
								int actualPower = Board.incrasePower(loc, +1);

								if (actualPower % 5 == 0) {
									String sendString = "<g>Erobern [<a>";
									for (int x = 0; x < 60; x++) {
										int length = Conf.warConquestTime / 60 * x;

										if (length >= actualPower && length < actualPower + 2) {
											sendString = sendString + "<n>";
										}
										sendString = sendString + "|";
									}
									sendString = sendString + "<g>]  " + actualPower;

									for (FPlayer sendPlayer : FPlayers.i.get()) {
										if (sendPlayer.getLastStoodAt().getDistanceTo(loc) < 1) {
											sendPlayer.msg(sendString);
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
}
