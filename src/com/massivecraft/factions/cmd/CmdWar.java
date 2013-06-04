package com.massivecraft.factions.cmd;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FWar;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Relation;

public class CmdWar extends FCommand {
	// Befehl um einen Krieg zu starten mit Forderungen

	public CmdWar() {
		super();
		this.aliases.add("war");

		this.requiredArgs.add("target");
		this.optionalArgs.put("cmd", "");
		this.optionalArgs.put("value", "");

		this.permission = Permission.MOD.node;
		this.disableOnLock = true;

		senderMustBePlayer = true;
		senderMustBeMember = true;
		senderMustBeModerator = true;
		senderMustBeAdmin = true;
	}

	@Override
	public void perform() {
		if (Conf.fwarEnabled) {
			Faction argFaction = this.argAsFaction(0);
			String argCmd = this.argAsString(1);
			int argValue = 0;
			if (this.argAsString(2) != null) {
				argValue = Integer.parseInt(this.argAsString(2));
			}

			if (argFaction != null) {
				if (argFaction.isNormal()) {
					boolean isAttacker = FWar.get(fme.getFaction(), argFaction) != null;
					boolean isTarget = FWar.get(argFaction, fme.getFaction()) != null;

					/* War doesn't exist */
					if (!isAttacker && !isTarget) {
						if (argCmd == null) { // Check that the cmd parameter is
												// empty
							if (argFaction.getRelationTo(fme.getFaction()) != Relation.ALLY) {
								if (argFaction.isAfterWarProtected(fme.getFaction()) == false) {
									if (!argFaction.getBeginnerProtection()) {
										if (!fme.getFaction().getBeginnerProtection()) {
											if (!argFaction.getTag().equalsIgnoreCase(fme.getFaction().getTag())) {
												/* Start the demands */
												new FWar(fme.getFaction(), argFaction);

												/* Send help messages */
												me.sendMessage(ChatColor.GOLD + "Du bist dabei einen Krieg gegen die Fraktion " + ChatColor.GREEN + argFaction.getTag() + ChatColor.GOLD
														+ " zu starten!");

												me.sendMessage(ChatColor.GOLD + "Folgende Befehle brauchst Du:");
												me.sendMessage(ChatColor.GREEN + " /f war " + argFaction.getTag() + " additems" + ChatColor.GOLD + " - F�ge Items zu den Forderungen hinzu");
												if (Conf.econEnabled) {
													me.sendMessage(ChatColor.GREEN + " /f war " + argFaction.getTag() + " addmoney  [money]" + ChatColor.GOLD + " - F�ge Geld zu den Forderungen hinzu");
												}
												me.sendMessage(ChatColor.GREEN + " /f war " + argFaction.getTag() + " cancel" + ChatColor.GOLD + " - Bricht die Forderungen/Krieg ab");
												me.sendMessage(ChatColor.GREEN + " /f war " + argFaction.getTag() + " confirm" + ChatColor.GOLD
														+ " - Best�tigt die Forderungen und informiert die gegnerische Fraktion");

												me.sendMessage(ChatColor.GOLD
														+ "Alle Items die Du zu den Forderungen hinzuf�gst werden eurer Fraktion abgezogen. Nach dem Krieg erhaltet ihr diese zur�ck.");
											} else {
												me.sendMessage(ChatColor.RED + "Du kannst deiner eigenen Fraktion keinen Krieg erkl�ren!");
											}
										} else {
											me.sendMessage(ChatColor.RED + "Deine Fraktion hat noch Anf�ngerschutz");
										}
									} else {
										me.sendMessage(ChatColor.RED + "Die Fraktion " + ChatColor.GOLD + argFaction.getTag() + ChatColor.RED + " hat noch Anf�ngerschutz");
									}
								} else {
									me.sendMessage(ChatColor.RED + "Ihr m�sst nach einem durch Forderungen beendeten Krieg " + Conf.fwarDaysAfterWarProtection
											+ " Tage warten bis ihr sie wieder angreifen k�nnt!");
								}
							} else {
								me.sendMessage(ChatColor.RED + "Ihr seid mit " + ChatColor.GOLD + this.argAsString(0) + ChatColor.RED + " verb�ndet!");
							}
						} else {
							me.sendMessage(ChatColor.RED + "Du hast noch keine Forderungen gegen " + ChatColor.GOLD + this.argAsString(0) + ChatColor.RED + " gestartet!");
						}
					}

					/* Faction is the attacker */
					if (isAttacker) {
						FWar fwar = FWar.get(fme.getFaction(), argFaction);

						if (!fwar.isStarted) { // If the war isn't started yet
							/* Check the commands */
							if (argCmd != null) {
								if (argCmd.equalsIgnoreCase("additems")) {
									Inventory inv = Bukkit.createInventory(me, 54, "Forderungen gegen " + fwar.getTargetFaction().getTag());
									InventoryView view = me.openInventory(inv);
									fwar.addTempInventory(view);
								}

								else if (argCmd.equalsIgnoreCase("addmoney")) {
									if (Conf.econEnabled) {
										if (argValue > 0) {
											if (Econ.modifyMoney(fme.getFaction(), -argValue, "", "for add money to a demand")) {
												fwar.money = fwar.money + argValue;
											}
										} else {
											me.sendMessage(ChatColor.RED + "Gebe einen Wert h�her als 0 an!");
										}
									} else {
										me.sendMessage(ChatColor.RED + "Economy support ist auf diesem Server nicht aktiviert!");
									}
								}

								else if (argCmd.equalsIgnoreCase("cancel")) {
									me.sendMessage(ChatColor.GREEN + "Du hast die Forderungen erfolgreich abgebrochen. Alle Items wurden eurem Itemkonto hinzugef�gt. " + ChatColor.GOLD
											+ "Auf dieses kannst du mit /f inventory" + ChatColor.GREEN + " zugreifen.");

									fwar.remove();

									// TODO: F�ge Items zu dem Itemkonte hinzu
								}

								else if (argCmd.equalsIgnoreCase("confirm")) {
									fwar.startWar();
								}

								else {
									me.sendMessage(ChatColor.RED + "Dieser Befehl existiert nicht!");
								}

							} else {
								me.sendMessage(ChatColor.RED + "Du hast bereits eine Forderung gestartet!");
							}
						} else {
							/* Check the commands */
							if (argCmd != null) {

								if (argCmd.equalsIgnoreCase("cancelwar")) {
									if (Conf.econEnabled) {
										Econ.modifyMoney(fwar.getTargetFaction(), fwar.moneyFromTarget, "because " + fme.getFaction().getTag(), "");
									}

									for (String matString : fwar.items.keySet()) {
										boolean exist = false;
										for (String matString2 : fwar.itemsToPay.keySet()) {
											if (matString.equals(matString2)) {
												exist = true;

												int amount = fwar.items.get(matString) - fwar.itemsToPay.get(matString2);

												if (amount > 0) {
													fwar.getTargetFaction().addItemsToInventory(matString, amount);
												}
											}
										}

										if (!exist) {
											fwar.getTargetFaction().addItemsToInventory(matString, fwar.items.get(matString));
										}
									}

									fwar.remove();

									fwar.getAttackerFaction().sendMessage(
											ChatColor.GOLD + "Der Krieg gegen " + ChatColor.GREEN + fwar.getTargetFaction().getTag() + ChatColor.GOLD + " wurde durch euch beendet!");
									fwar.getTargetFaction().sendMessage(
											ChatColor.GOLD + "Der Krieg gegen " + fwar.getAttackerFaction().getTag() + ChatColor.GOLD + " wurde durch die Angreifer beendet!");

								}

							} else {
								if (!fwar.isWar) {
									me.sendMessage(ChatColor.GOLD + "Zeit bis zum Start des Krieges: " + ChatColor.GREEN + fwar.getTimeToWar());
								} else {
									me.sendMessage(ChatColor.GOLD + "Ihr befindet euch bereits im Krieg mit der fraktion: " + ChatColor.GREEN + fwar.getTargetFaction().getTag());
								}
							}
						}
					}

					/* Faction is the target */
					if (isTarget) {
						FWar fwar = FWar.get(argFaction, fme.getFaction());

						if (fwar.isStarted) { // If the war isn't started yet
							/* Check the commands */
							if (argCmd != null) {
								if (argCmd.equalsIgnoreCase("payitems")) {
									Inventory inv = Bukkit.createInventory(me, 54, "Forderungen von " + fwar.getAttackerFaction().getTag());

									/* Add Content */
									for (String matString : fwar.itemsToPay.keySet()) {
										MaterialData mat = FWar.convertStringToMaterialData(matString);

										if (fwar.itemsToPay.get(matString) != null) {
											int amount = fwar.itemsToPay.get(matString);
											if (amount > 0) {
												ItemStack item = new ItemStack(mat.getItemType(), amount, mat.getData());
												inv.addItem(item);
											}
										}
									}

									InventoryView view = me.openInventory(inv);
									fwar.addTempInventoryFromTarget(view);
								}

								else if (argCmd.equalsIgnoreCase("paymoney")) {
									if (Conf.econEnabled) {
										if (Econ.modifyMoney(fme.getFaction(), -fwar.money, "", "for add money to pay a demand")) {
											fwar.moneyFromTarget = fwar.money;
										}
									} else {
										me.sendMessage(ChatColor.RED + "Economy support ist auf diesem Server nicht aktiviert!");
									}
								}

								else if (argCmd.equalsIgnoreCase("cancelpay")) {
									/* Money */
									if (Conf.econEnabled) {
										Econ.modifyMoney(fme.getFaction(), fwar.moneyFromTarget, "for cancelling the pay for a war", "");
									}

									/* Items */
									for (String matString : fwar.items.keySet()) {
										boolean exist = false;
										for (String matString2 : fwar.itemsToPay.keySet()) {
											if (matString.equals(matString2)) {
												exist = true;

												int amount = fwar.items.get(matString) - fwar.itemsToPay.get(matString2);

												if (amount > 0) {
													fwar.getTargetFaction().addItemsToInventory(matString, amount);
												}
											}
										}

										if (!exist) {
											fwar.getTargetFaction().addItemsToInventory(matString, fwar.items.get(matString));
										}
									}

									fwar.itemsToPay = new HashMap<String, Integer>(fwar.items);

									/* Message */
									me.sendMessage(ChatColor.GREEN + "Du hast die Zahlung der Forderungen erfolgreich abgebrochen. Alle Items wurden eurem Itemkonto hinzugef�gt.");
								}

								else if (argCmd.equalsIgnoreCase("info")) {
									me.sendMessage(ChatColor.RED + "Zu Entrichten: " + fwar.getDemandsAsString());
								}

								else if (argCmd.equalsIgnoreCase("confirmpay")) {
									int counterForOutput = 0;
									String outputString = "";

									/* Check Materials */
									boolean passed = true;
									for (String mat : fwar.itemsToPay.keySet()) {
										int amount = fwar.itemsToPay.get(mat);
										if (amount > 0) {
											passed = false;

											outputString = outputString + (amount + " " + mat + ", ");
										}
									}

									/* Check Money */
									if (Conf.econEnabled) {
										if (fwar.money > fwar.moneyFromTarget) {
											counterForOutput++;
											if (counterForOutput > 2) {
												outputString = outputString + "/";
											}
											outputString = outputString + Econ.moneyString(fwar.money - fwar.moneyFromTarget);

											passed = false;
										}
									}

									if (passed == true) {
										me.sendMessage(ChatColor.GOLD + "Forderungen wurden Erf�llt!");

										/* Add items to the attackers inventory */
										for (String mat : fwar.items.keySet()) {
											int amount = fwar.items.get(mat);

											fwar.getAttackerFaction().addItemsToInventory(mat, amount * 2); // Duplicate
																											// the
																											// amount
										}

										if (Conf.econEnabled) {
											Econ.modifyMoney(fwar.getAttackerFaction(), fwar.money, "for wining a war on demand", "");
											Econ.modifyMoney(fwar.getAttackerFaction(), fwar.moneyFromTarget, "for wining a war on demand", "");
										}

										fwar.getTargetFaction().factionsAfterWarProtection.put(fwar.getAttackerFaction().getId(), System.currentTimeMillis());

										fwar.remove();

										fwar.getAttackerFaction().sendMessage(
												ChatColor.GOLD + "Der Krieg gegen " + ChatColor.GREEN + fwar.getTargetFaction().getTag() + ChatColor.GOLD
														+ " wurde Beendet durch das Zahlen der Vorderungen!");
										fwar.getTargetFaction().sendMessage(
												ChatColor.GOLD + "Der Krieg gegen " + fwar.getAttackerFaction().getTag() + ChatColor.GOLD + " wurde Beendet durch das Zahlen der Vorderungen!");
									} else {
										me.sendMessage(ChatColor.GOLD + "Um die Vorderungen zu bezahen fehlen noch: " + ChatColor.GREEN + ChatColor.RED + outputString + "!");
									}
								}

								else {
									me.sendMessage(ChatColor.RED + "Dieser Befehl existiert nicht!");
								}
							}
						} else {
							me.sendMessage(ChatColor.RED + "Die Fraktion " + ChatColor.GOLD + fwar.getAttackerFaction().getTag() + ChatColor.RED
									+ " ist gerade dabei einen Krieg gegen euch zu starten! " + ChatColor.WHITE
									+ "Sollten sie nach 30 Minuten nicht fertig sein, so k�nnt ihr einen Krieg gegen sie starten.");
						}
					}
				} else {
					me.sendMessage(ChatColor.RED + "Du kannst keinen Krieg gegen " + ChatColor.GOLD + argFaction.getTag() + ChatColor.RED + " starten!");
				}
			} else {
				me.sendMessage(ChatColor.GREEN + "[Factions Kriegssystem]:");
				me.sendMessage(ChatColor.GOLD + "Hier eine Kurze Erkl�rung:");
				me.sendMessage(ChatColor.GOLD + "Einen Krieg startest du mit " + ChatColor.GREEN + "/f war <Fraktionsname>");
				me.sendMessage(ChatColor.GOLD
						+ "Dadurch wirst du zum Angreifer. Angreifer haben den Vorteil das sie den Krieg immer beenden k�nnen. Ihr Nachteil besteht darin, dass wenn sie eine Fraktion mit weniger Spielern angreifen, so m�ssen sie einen T�glichen Ersatz entrichten. Dieser Ersatz wird "
						+ ChatColor.RED + "NICHT" + ChatColor.GOLD + " den Verteidigern zugesagt.");
				me.sendMessage(ChatColor.GOLD + "Der Zu entrichtende Ersatz berechnet sich wie folgt: " + ChatColor.GREEN
						+ "((Anzahl Spieler der Angreifer)-(Anzahl Spieler der Verteidiger))*10 Pro Tag.");
				me.sendMessage(ChatColor.GOLD
						+ "Dieses Kriegssystem basiert auf dem Fordern von Items und Geld. Dem Verteidiger ist es somit m�glich den Krieg durch das bezahlen der Forderungen zu beenden.");
				me.sendMessage(ChatColor.GOLD + "Beendet der Verteidiger den Krieg durch das bezahlen der Forderungen so ist er f�r " + Conf.fwarDaysAfterWarProtection
						+ " Tage gegen erneuten Krieg mit den Angreifern gesch�tzt.");
				me.sendMessage(ChatColor.YELLOW + "Verf�gbare Befehle:");
				me.sendMessage(ChatColor.GREEN + "Als Angreifer:");
				me.sendMessage(ChatColor.GREEN + "Vorbereitungs Phase:");
				me.sendMessage(ChatColor.YELLOW + "/f war <Fraktionsname> additems" + ChatColor.GOLD + " F�gt Items den Forderungen gegen�ber der Anzugreienden Fraktion hinzu");
				me.sendMessage(ChatColor.YELLOW + "/f war <Fraktionsname> addmoney" + ChatColor.GOLD + " F�gt Geld den Forderungen gegen�ber der Anzugreienden Fraktion hinzu");
				me.sendMessage(ChatColor.YELLOW + "/f war <Fraktionsname> cancel" + ChatColor.GOLD + " Bricht die Vorbereitungs Phase und den Krieg ab.");
				me.sendMessage(ChatColor.YELLOW + "/f war <Fraktionsname> confirm" + ChatColor.GOLD + " Best�tigt die gestellten Forderungen uns Startet einen Countdown von "
						+ Conf.fwarHoursUntilWarStartsAfterDemand + " Stunden bis der Krieg ausbricht");
				me.sendMessage(ChatColor.GREEN + "W�hrend dem Krieg:");
				me.sendMessage(ChatColor.YELLOW + "/f war <Fraktionsname> cancelwar" + ChatColor.GOLD + " Beendet den Krieg");
				me.sendMessage(ChatColor.GREEN + "Als Verteidiger:");
				me.sendMessage(ChatColor.YELLOW + "/f war <Fraktionsname> payitems" + ChatColor.GOLD + " Bereitet die Zahlung der Items vor.");
				me.sendMessage(ChatColor.YELLOW + "/f war <Fraktionsname> paymoney" + ChatColor.GOLD + " Bereitet die Zahlung des Geldes vor.");
				me.sendMessage(ChatColor.YELLOW + "/f war <Fraktionsname> cancelpay" + ChatColor.GOLD + " Bricht den Bezahlungsprozess ab");
				me.sendMessage(ChatColor.YELLOW + "/f war <Fraktionsname> confirmpay" + ChatColor.GOLD + " Best�tigt das Bezahlen der Items und des Geldes. Beendet den Krieg. Und leitet den "
						+ Conf.fwarDaysAfterWarProtection + " Tage Schutz den Angreifern Gegen�ber ein.");
			}
		}
	}
}
