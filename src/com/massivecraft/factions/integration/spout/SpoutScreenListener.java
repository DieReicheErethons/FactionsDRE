package com.massivecraft.factions.integration.spout;

import java.util.ArrayList;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.getspout.spoutapi.event.screen.*;
import org.getspout.spoutapi.gui.Button;
import org.getspout.spoutapi.gui.GenericButton;
import org.getspout.spoutapi.gui.GenericTexture;
import org.getspout.spoutapi.gui.ScreenType;
import org.getspout.spoutapi.player.SpoutPlayer;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.P;
import com.massivecraft.factions.struct.Role;

public class SpoutScreenListener implements Listener {

	@EventHandler(priority = EventPriority.NORMAL)
	public void onScreenOpen(ScreenOpenEvent event) {
		SpoutPlayer sPlayer = event.getPlayer();
		FPlayer player = FPlayers.i.get(sPlayer.getPlayer());

		ScreenType screentype = event.getScreenType();

		if (screentype == ScreenType.CHAT_SCREEN) {
			if (player != null) {
				if (player.getsMenu() != null) {
					player.getsMenu().enableChatMenu(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onButtonClick(ButtonClickEvent event) {
		Button Button = event.getButton();
		SpoutPlayer sPlayer = event.getPlayer();
		FPlayer player = FPlayers.i.get(sPlayer.getPlayer());

		if (player == null || sPlayer == null) {
			return;
		}

		if (player.getsMenu() == null) {
			return;
		}

		// Check CloseButton
		if (Button == player.getsMenu().CloseButton) {
			player.getsMenu().menuPopup.close();
			sPlayer.openScreen(ScreenType.GAME_SCREEN);
		}

		// Check Menüleiste----------
		if (Button == player.getsMenu().MainButton) {
			player.getsMenu().changeMenu(0);
		} else if (Button == player.getsMenu().DiplomatieButton) {
			player.getsMenu().changeMenu(1);
		} else if (Button == player.getsMenu().BankButton) {
			player.getsMenu().changeMenu(2);
		} else if (Button == player.getsMenu().AdminButton) {
			player.getsMenu().changeMenu(3);
		} else if (Button == player.getsMenu().BuildingButton) {
			player.getsMenu().changeMenu(4);
		}

		// Check Hauptmenü------------
		if (Button == player.getsMenu().faction_create) {
			createFaction(player, player.getsMenu().faction_createtext.getText());
			player.getsMenu().changeMenu(0);
		}

		if (Button == player.getsMenu().faction_PageBack) {
			player.getsMenu().changeFactionPage(player.getsMenu().faction_CurrentPage - 1);
		}

		if (Button == player.getsMenu().faction_PageForward) {
			player.getsMenu().changeFactionPage(player.getsMenu().faction_CurrentPage + 1);
		}

		// Factionlist
		for (GenericButton flbutton : player.getsMenu().factionlist_buttons) {
			if (Button == flbutton) {
				player.getsMenu().showFactionMenu(Button.getText());
			}
		}

		if (Button == player.getsMenu().faction_join) {
			player.join(Factions.i.getByTag(player.getsMenu().faction_name.getText()));
			player.getsMenu().changeMenu(0); // Update

		} else if (Button == player.getsMenu().faction_leave) {
			player.leave(true);
			player.getsMenu().changeMenu(0); // Update
		}

		if (Button == player.getsMenu().faction_membersbutton) {
			player.getsMenu().faction_membersbox.setVisible(true);
			player.getsMenu().faction_membersbutton.setEnabled(false);
			player.getsMenu().faction_alliesbox.setVisible(false);
			player.getsMenu().faction_alliesbutton.setEnabled(true);
			player.getsMenu().faction_enemiesbox.setVisible(false);
			player.getsMenu().faction_enemiesbutton.setEnabled(true);
		}

		if (Button == player.getsMenu().faction_alliesbutton) {
			player.getsMenu().faction_membersbox.setVisible(false);
			player.getsMenu().faction_membersbutton.setEnabled(true);
			player.getsMenu().faction_alliesbox.setVisible(true);
			player.getsMenu().faction_alliesbutton.setEnabled(false);
			player.getsMenu().faction_enemiesbox.setVisible(false);
			player.getsMenu().faction_enemiesbutton.setEnabled(true);
		}

		if (Button == player.getsMenu().faction_enemiesbutton) {
			player.getsMenu().faction_membersbox.setVisible(false);
			player.getsMenu().faction_membersbutton.setEnabled(true);
			player.getsMenu().faction_alliesbox.setVisible(false);
			player.getsMenu().faction_alliesbutton.setEnabled(true);
			player.getsMenu().faction_enemiesbox.setVisible(true);
			player.getsMenu().faction_enemiesbutton.setEnabled(false);
		}

		// Check Diplomatie Menü----------

		if (Button == player.getsMenu().diplomatie_neutral) {

		}

		if (Button == player.getsMenu().diplomatie_ally) {

		}

		if (Button == player.getsMenu().diplomatie_enemy) {

		}

		// Check AdminMenu----------------

		for (GenericButton acbutton : player.getsMenu().admin_flagcolor.keySet()) {

			if (Button == acbutton) {
				int type = Integer.parseInt(acbutton.getText()) + 1;
				if (type > 15)
					type = 0;

				int index = acbutton.getMinWidth() * 2 + acbutton.getMinHeight();

				GenericTexture actexture = player.getsMenu().admin_flagcolor.get(acbutton);
				actexture.setUrl("http://dl.dropbox.com/u/14933646/Server/color_" + type + ".png");

				player.getFaction().setFlag(index, type);

				acbutton.setText("" + type);
			}
		}

		// Check BuildingsMenu--------------
		for (GenericButton btbutton : player.getsMenu().BType_Buttons.keySet()) {
			if (Button == btbutton) {
				player.getsMenu().menuPopup.close();
				player.setIsBuilding(player.getsMenu().BType_Buttons.get(btbutton));
			}
		}

	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onSliderDrag(SliderDragEvent event) {

	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onTextFieldChange(TextFieldChangeEvent event) {

	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onScreenClose(ScreenCloseEvent event) {
		SpoutPlayer sPlayer = event.getPlayer();
		FPlayer player = FPlayers.i.get(sPlayer.getPlayer());

		ScreenType screen = event.getScreenType();

		if (screen == ScreenType.CHAT_SCREEN) {
			player.getsMenu().enableChatMenu(false);
		}
	}

	// Eigene Befehle
	private void createFaction(FPlayer fme, String tag) {
		if (fme.hasFaction()) {
			fme.msg("<b>You must leave your current faction first.");
			return;
		}

		if (Factions.i.isTagTaken(tag)) {
			fme.msg("<b>That tag is already in use.");
			return;
		}

		ArrayList<String> tagValidationErrors = Factions.validateTag(tag);
		if (tagValidationErrors.size() > 0) {
			fme.sendMessage(tagValidationErrors);
			return;
		}

		// if economy is enabled, they're not on the bypass list, and this
		// command has a cost set, make 'em pay
		if (!fme.payForCommand(Conf.econCostCreate, "to create a new faction", "for creating a new faction"))
			return;

		Faction faction = Factions.i.create();

		if (faction == null) {
			fme.msg("<b>There was an internal error while trying to create your faction. Please try again.");
			return;
		}

		faction.setTag(tag);
		fme.setRole(Role.ADMIN);
		fme.setFaction(faction);

		faction.setBeginnerProtection(true);
		faction.setBeginnerProtectionTime(System.currentTimeMillis() + Conf.warBeginnerProtection * 60 * 60 * 1000);

		for (FPlayer follower : FPlayers.i.getOnline()) {
			follower.msg("%s<i> created a new faction %s", fme.describeTo(follower, true), faction.getTag(follower));
		}

		fme.msg("<i>You should now: %s", P.p.cmdBase.cmdDescription.getUseageTemplate());

		if (Conf.logFactionCreate)
			P.p.log(fme.getName() + " created a new faction: " + tag);
	}

}
