package com.massivecraft.factions.integration.spout;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.getspout.spoutapi.gui.*;
import org.getspout.spoutapi.player.SpoutPlayer;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.P;
import com.massivecraft.factions.buildings.BuildingType;
import com.massivecraft.factions.struct.Role;

public class SpoutMenu {
	// Colors
	private Color blue = new Color(0, 0, 255);
	private Color red = new Color(255, 0, 0);
	private Color green = new Color(0, 255, 0);
	private Color white = new Color(255, 255, 255);

	// Player
	public FPlayer fplayer;
	private SpoutPlayer splayer;

	// Menüs
	public GenericPopup menuPopup;

	public GenericBox MainBox, BankBox, DiplomatieBox, AdminBox, BuildingBox;

	// Close-Button
	public GenericButton CloseButton;

	// Top-Menu Buttons
	public GenericButton MainButton, BankButton, DiplomatieButton, AdminButton, BuildingButton;

	// Functions
	public SpoutMenu(FPlayer player, SpoutPlayer splayer) {
		this.fplayer = player;
		this.splayer = splayer;
		this.initFactionsMenu();
	}

	// Factions Menü ################################
	public void initFactionsMenu() {
		menuPopup = new GenericPopup();

		CloseButton = new GenericButton("X");
		CloseButton.setWidth(15).setHeight(15).setY(5).setX(5);

		GenericLabel title = new GenericLabel(P.p.getDescription().getFullName());
		title.setAlign(WidgetAnchor.TOP_CENTER).setAnchor(WidgetAnchor.TOP_CENTER).setWidth(200).setHeight(30).setY(5);

		GenericTexture titlebild = new GenericTexture(
				"https://wunderkit-files.s3.amazonaws.com/VYFXrsanTNmDDwVA7dRQhm42pNMX0utcnPhnpD3plEk20120202155457/avatars/large_914-3ee6fda6f19509468b5b5226f1df38d8792f55a1.png");
		titlebild.setWidth(30).setHeight(30).setY(splayer.getMainScreen().getHeight() - 30).setX(2);

		MainButton = new GenericButton("Hauptmenü");
		MainButton.setWidth(60).setHeight(15).setY(30).setX(5);

		BankButton = new GenericButton("Bank");
		BankButton.setWidth(60).setHeight(15).setY(50).setX(5);

		DiplomatieButton = new GenericButton("Diplomatie");
		DiplomatieButton.setWidth(60).setHeight(15).setY(70).setX(5);

		AdminButton = new GenericButton("Admin");
		AdminButton.setWidth(60).setHeight(15).setY(90).setX(5);

		BuildingButton = new GenericButton("Gebäude");
		BuildingButton.setWidth(60).setHeight(15).setY(110).setX(5);

		menuPopup.attachWidget(P.p, title).attachWidget(P.p, titlebild).attachWidget(P.p, MainButton).attachWidget(P.p, BankButton).attachWidget(P.p, DiplomatieButton).attachWidget(P.p, AdminButton)
				.attachWidget(P.p, BuildingButton).attachWidget(P.p, CloseButton);

		initMainMenu();
		initBankMenu();
		initDiplomatieMenu();
		initAdminMenu();
		initBuildingMenu();

		// refresh FactionsMenu
		refreshFactionsMenu();

	}

	public void refreshFactionsMenu() {
		// Modberechtigungen
		boolean isAdmin = false;
		if (fplayer == fplayer.getFaction().getFPlayerAdmin() || fplayer.isAdminBypassing() || fplayer.getFaction().getFPlayersWhereRole(Role.MODERATOR).contains(fplayer)) {
			isAdmin = true;
		}

		if (fplayer.hasFaction()) {
			BankButton.setEnabled(false);
			if (isAdmin) {
				DiplomatieButton.setEnabled(false);
				BuildingButton.setEnabled(false);
				AdminButton.setEnabled(true);
			} else {
				DiplomatieButton.setEnabled(false);
				BuildingButton.setEnabled(false);
				AdminButton.setEnabled(false);
			}

		} else {
			BankButton.setEnabled(false);
			DiplomatieButton.setEnabled(false);
			BuildingButton.setEnabled(false);
			AdminButton.setEnabled(false);
		}
	}

	//

	// Main Menü
	public CopyOnWriteArrayList<GenericButton> factionlist_buttons;
	public GenericContainer factionlist;
	public GenericButton faction_create;
	public GenericTextField faction_createtext;

	public GenericLabel faction_ShowPage;
	public GenericButton faction_PageForward;
	public GenericButton faction_PageBack;

	private void initMainMenu() {
		MainBox = new GenericBox();
		MainBox.addPopup(menuPopup).setY(20).setX(80);

		// Factionless Menu
		GenericLabel title = new GenericLabel("Fraktions Liste");
		title.setWidth(70).setHeight(20);
		MainBox.addWidget(title);

		factionlist = new GenericContainer();
		factionlist.setWidth(70).setHeight(0).setY(10);
		MainBox.addWidget(factionlist);

		factionlist_buttons = new CopyOnWriteArrayList<GenericButton>();

		faction_PageBack = new GenericButton("<--");
		faction_PageBack.setWidth(20).setY(splayer.getMainScreen().getHeight() - 70).setHeight(15);
		MainBox.addWidget(faction_PageBack);

		faction_ShowPage = new GenericLabel("1/1");
		faction_ShowPage.setWidth(20).setX(25).setY(splayer.getMainScreen().getHeight() - 65).setHeight(15);
		MainBox.addWidget(faction_ShowPage);

		faction_PageForward = new GenericButton("-->");
		faction_PageForward.setWidth(20).setX(50).setY(splayer.getMainScreen().getHeight() - 70).setHeight(15);
		MainBox.addWidget(faction_PageForward);

		this.changeFactionPage(0);

		// Faction Create Button
		faction_create = new GenericButton("Neue Fraktion Erstellen");
		faction_create.setWidth(130).setX(splayer.getMainScreen().getWidth() - 295).setY(splayer.getMainScreen().getHeight() - 40).setHeight(15);
		MainBox.addWidget(faction_create);
		if (Conf.econEnabled && Conf.econCostCreate != 0)
			faction_create.setTooltip(Conf.econCostCreate + "$");
		// Textfield
		faction_createtext = new GenericTextField();
		faction_createtext.setMaximumCharacters(10).setText("Namen").setY(splayer.getMainScreen().getHeight() - 40).setWidth(splayer.getMainScreen().getWidth() - 295 - 5).setHeight(15);
		MainBox.addWidget(faction_createtext);

		if (fplayer.hasFaction()) {
			faction_create.setEnabled(false);
			faction_createtext.setEnabled(false);
		}

		// TmpButtons Box die disabled werden müssen
		faction_tmplabels = new GenericBox();
		faction_tmplabels.addPopup(menuPopup);

		// Eigenes Factionmenu aktivieren
		showFactionMenu(fplayer.getFaction().getTag());

	}

	public int faction_MaxButtonsPerPage = 10;
	public int faction_CurrentPage = 0;

	public void changeFactionPage(int index) {
		int maxpages = (int) Math.floor(Factions.i.get().size() / faction_MaxButtonsPerPage);

		if (index < 0)
			index = 0;

		if (index > maxpages)
			index = maxpages;

		faction_CurrentPage = index;

		// Sort List
		ArrayList<Faction> factionList = new ArrayList<Faction>(Factions.i.get());
		factionList.remove(Factions.i.getNone());
		factionList.remove(Factions.i.getSafeZone());
		factionList.remove(Factions.i.getWarZone());
		factionList.remove(Factions.i.getNeutralZone());

		// Sort by total followers first
		Collections.sort(factionList, new Comparator<Faction>() {
			@Override
			public int compare(Faction f1, Faction f2) {
				int f1Size = f1.getFPlayers().size();
				int f2Size = f2.getFPlayers().size();
				if (f1Size < f2Size)
					return 1;
				else if (f1Size > f2Size)
					return -1;
				return 0;
			}
		});

		// Then sort by how many members are online now
		Collections.sort(factionList, new Comparator<Faction>() {
			@Override
			public int compare(Faction f1, Faction f2) {
				int f1Size = f1.getFPlayersWhereOnline(true).size();
				int f2Size = f2.getFPlayersWhereOnline(true).size();
				if (f1Size < f2Size)
					return 1;
				else if (f1Size > f2Size)
					return -1;
				return 0;
			}
		});

		// Draw Faction Buttons
		for (Widget button : factionlist.getChildren()) {
			factionlist.removeChild(button);
		}

		int height = 0, i = 0;
		for (Faction faction : factionList) {
			i++;
			if (i > index * faction_MaxButtonsPerPage && i <= index * faction_MaxButtonsPerPage + faction_MaxButtonsPerPage) {
				height = height + 15;
				GenericButton labftag = new GenericButton(faction.getTag());
				factionlist.addChild(labftag);
				labftag.setHeight(15).setWidth(70);
				factionlist_buttons.add(labftag);

				// Facionstatus checken
				if (fplayer.hasFaction()) {
					if (faction.getRelationTo(fplayer.getFaction()).isAlly()) {
						labftag.setColor(blue);
						labftag.setDisabledColor(blue);
					}
					if (faction.getRelationTo(fplayer.getFaction()).isEnemy()) {
						labftag.setColor(red);
						labftag.setDisabledColor(red);
					}
					if (fplayer.getFaction() == faction) {
						labftag.setColor(green);
						labftag.setDisabledColor(green);
					}
				}
			}
		}
		factionlist.updateLayout().updateSize().setHeight(height);

		// Set the Page Buttons / Label
		faction_ShowPage.setText((index + 1) + "/" + (maxpages + 1));

		if (index == 0)
			faction_PageBack.setEnabled(false);
		else
			faction_PageBack.setEnabled(true);

		if (index == maxpages)
			faction_PageForward.setEnabled(false);
		else
			faction_PageForward.setEnabled(true);
	}

	public void refreshMain() {

		for (GenericButton labftag : factionlist_buttons) {
			if (Factions.i.getByTag(labftag.getText()) == null) {
				factionlist_buttons.remove(labftag);
				factionlist.removeChild(labftag);
				factionlist.setHeight(factionlist.getHeight() - 15);
				if (faction_name.getText().equals(labftag.getText())) {
					showFactionMenu(null); // Current Faction Menu Disabled
				}
			}

		}

		changeFactionPage(faction_CurrentPage);

		// Faction Create Button
		faction_create.setWidth(130).setY(splayer.getMainScreen().getHeight() - 40).setHeight(15).setX(splayer.getMainScreen().getWidth() - 295);
		MainBox.addWidget(faction_create);
		if (Conf.econEnabled && Conf.econCostCreate != 0)
			faction_create.setTooltip(Conf.econCostCreate + "$");
		if (fplayer.hasFaction()) {
			faction_create.setEnabled(false);
			faction_createtext.setEnabled(false);
		} else {
			faction_create.setEnabled(true);
			faction_createtext.setEnabled(true);
		}

		// Eigenes Factionmenu aktivieren
		showFactionMenu(fplayer.getFaction().getTag());
	}

	// Bank Menü
	private void initBankMenu() {
		/*
		 * MainTitle = new
		 * GenericLabel("Bank");menuContainerBank.addChild(title);
		 * title.setAlign
		 * (WidgetAnchor.TOP_CENTER).setAnchor(WidgetAnchor.TOP_CENTER
		 * ).setWidth(200).setHeight(30);
		 * 
		 * GenericGradient gradient = new
		 * GenericGradient();menuContainerBank.addChild(gradient);
		 * gradient.setTopColor(new Color(1.0F, 1.0F, 1.0F, 1.0F)); // White
		 * (order is Red, Green, Blue, Alpha) gradient.setBottomColor(new
		 * Color(1.0F, 0, 0, 1.0F)); // Red gradient.setHeight(32).setWidth(32);
		 * 
		 * menuPopup.attachWidget(P.p,menuContainerBank);
		 */
	}

	// Diplomatie Menü
	public GenericButton diplomatie_ally, diplomatie_enemy, diplomatie_neutral;

	private void initDiplomatieMenu() {
		DiplomatieBox = new GenericBox();
		DiplomatieBox.addPopup(menuPopup).setY(20).setX(80);

		diplomatie_neutral = new GenericButton("Nicht-Angriffs-Pakt");
		diplomatie_neutral.setX(120).setY(0).setWidth(100).setHeight(20);
		DiplomatieBox.addWidget(diplomatie_neutral);
		if (Conf.econEnabled || Conf.econCostNeutral != 0)
			diplomatie_neutral.setTooltip(Conf.econCostNeutral + "$");

		diplomatie_ally = new GenericButton("Bündniss eingehen");
		diplomatie_ally.setColor(blue).setX(120).setY(40).setWidth(100).setHeight(20);
		DiplomatieBox.addWidget(diplomatie_ally);
		if (Conf.econEnabled || Conf.econCostAlly != 0)
			diplomatie_ally.setTooltip(Conf.econCostAlly + "$");

		diplomatie_enemy = new GenericButton("Krieg erklären!");
		diplomatie_enemy.setColor(red).setX(120).setY(80).setWidth(100).setHeight(20);
		DiplomatieBox.addWidget(diplomatie_enemy);
		if (Conf.econEnabled || Conf.econCostEnemy != 0)
			diplomatie_enemy.setTooltip(Conf.econCostEnemy + "$");
	}

	public void refreshDiplomatieMenu() {
		changeFactionPage(faction_CurrentPage);
	}

	// Admin Menü
	public GenericButton admin_flag;
	public Map<GenericButton, GenericTexture> admin_flagcolor;

	private void initAdminMenu() {
		AdminBox = new GenericBox();
		AdminBox.addPopup(menuPopup).setY(20).setX(80);

		// Flagge
		admin_flag = new GenericButton("Flagge");
		admin_flag.setWidth(60).setHeight(20).setX(0).setY(0);
		AdminBox.addWidget(admin_flag);

		admin_flagcolor = new HashMap<GenericButton, GenericTexture>();
		for (int y = 0; y < 2; y++) {
			for (int x = 0; x < 3; x++) {
				GenericButton colorbutton = new GenericButton("0");
				colorbutton.setMinWidth(x).setMinHeight(y);
				colorbutton.setPriority(RenderPriority.High).setWidth(46).setHeight(46).setX(2 + x * 52).setY(2 + 25 + y * 52);
				AdminBox.addWidget(colorbutton);
				GenericTexture colortexture = new GenericTexture("http://dl.dropbox.com/u/14933646/Server/color_0.png");
				colortexture.setPriority(RenderPriority.Lowest).setWidth(50).setHeight(50).setX(x * 52).setY(25 + y * 52);
				AdminBox.addWidget(colortexture);

				admin_flagcolor.put(colorbutton, colortexture);

			}
		}

	}

	private void refreshAdminMenu() {
		for (GenericButton acbutton : this.admin_flagcolor.keySet()) {
			int index = acbutton.getMinWidth() * 2 + acbutton.getMinHeight();

			int type = this.fplayer.getFaction().getFlag(index);

			GenericTexture actexture = this.admin_flagcolor.get(acbutton);
			actexture.setUrl("http://dl.dropbox.com/u/14933646/Server/color_" + type + ".png");
		}
	}

	// Gebäude Menü
	public Map<GenericButton, BuildingType> BType_Buttons;

	private void initBuildingMenu() {
		BType_Buttons = new HashMap<GenericButton, BuildingType>();

		BuildingBox = new GenericBox();
		BuildingBox.addPopup(menuPopup).setY(20).setX(80);

		int y = 0;
		for (BuildingType btype : BuildingType.BuildingTypes) {
			y = y + 15;

			GenericButton button = new GenericButton(btype.name);
			button.setHeight(15).setWidth(90).setY(y);
			BuildingBox.addWidget(button);

			GenericTexture pic = new GenericTexture(btype.imagepath);
			pic.setWidth(15).setHeight(15).setY(y).setX(110);
			BuildingBox.addWidget(pic);

			BType_Buttons.put(button, btype);
		}

	}

	public void changeMenu(int menustate) {
		if (splayer.getMainScreen().getActivePopup() != menuPopup)
			splayer.getMainScreen().attachPopupScreen(menuPopup);

		switch (menustate) {
		case 0: // Menu 0, Main
			MainBox.show();
			BuildingBox.hide();
			DiplomatieBox.hide();
			AdminBox.hide();

			refreshMain();
			break;
		case 1: // Menu 1, Diplomatics
			MainBox.hide();
			BuildingBox.hide();
			DiplomatieBox.show();
			AdminBox.hide();
			refreshDiplomatieMenu();

			break;
		case 2: // Menu 2, Bank
			MainBox.hide();
			BuildingBox.hide();
			DiplomatieBox.hide();
			AdminBox.hide();
			break;
		case 3: // Menu 3, Admin
			refreshAdminMenu();
			MainBox.hide();
			BuildingBox.hide();
			DiplomatieBox.hide();
			AdminBox.show();
			break;
		case 4: // Menu 4, Buildings
			MainBox.hide();
			BuildingBox.show();
			DiplomatieBox.hide();
			AdminBox.hide();
			break;
		}

		// refresh FactionsMenu
		refreshFactionsMenu();

	}

	// Faction Menü
	public GenericLabel faction_name, faction_description, faction_power, faction_maxpower, faction_land;
	public GenericListWidget faction_membersbox, faction_alliesbox, faction_enemiesbox;
	public GenericButton faction_join, faction_leave;
	public GenericBox faction_tmplabels;
	public GenericButton faction_membersbutton, faction_enemiesbutton, faction_alliesbutton;
	public Set<GenericTexture> faction_flag = new HashSet<GenericTexture>();

	public void showFactionMenu(String tag) {
		// Löschen
		MainBox.removeWidget(faction_name).removeWidget(faction_description).removeWidget(faction_power).removeWidget(faction_maxpower).removeWidget(faction_land).removeWidget(faction_membersbutton)
				.removeWidget(faction_enemiesbutton).removeWidget(faction_alliesbutton).removeWidget(faction_join).removeWidget(faction_leave).removeWidget(faction_membersbox)
				.removeWidget(faction_alliesbox).removeWidget(faction_enemiesbox);
		for (GenericWidget tempwidget : faction_flag) {
			MainBox.removeWidget(tempwidget);
		}
		faction_flag = new HashSet<GenericTexture>();

		// Boxen löschen
		faction_tmplabels.remove();

		// Button deaktivieren
		for (GenericButton flbutton : factionlist_buttons) {
			if (flbutton.getText().equals(tag)) {
				flbutton.setEnabled(false);
			} else {
				flbutton.setEnabled(true);
			}
		}

		if (tag != null) {
			// Neu Erstellen
			Faction faction = Factions.i.getByTag(tag);

			GenericLabel tmplabel;

			if (faction != null) {
				if (faction.isNormal()) {
					// Main Info
					faction_name = new GenericLabel(tag);
					faction_name.setHeight(20).setX(90);
					MainBox.addWidget(faction_name);

					// Facionstatus checken
					if (fplayer.hasFaction()) {
						if (faction.getRelationTo(fplayer.getFaction()).isMember())
							faction_name.setTextColor(green);
						if (faction.getRelationTo(fplayer.getFaction()).isAlly())
							faction_name.setTextColor(blue);
						if (faction.getRelationTo(fplayer.getFaction()).isEnemy())
							faction_name.setTextColor(red);
					}

					faction_description = new GenericLabel(faction.getDescription());
					faction_description.setHeight(20).setX(90).setY(20);
					MainBox.addWidget(faction_description);

					// Land / Power / MaxPower
					tmplabel = new GenericLabel("Land");
					tmplabel.setHeight(20).setX(90).setY(40);
					MainBox.addWidget(tmplabel);
					faction_tmplabels.addWidget(tmplabel);

					faction_land = new GenericLabel("" + faction.getLandRounded());
					faction_land.setHeight(20).setX(90).setY(50);
					MainBox.addWidget(faction_land);

					tmplabel = new GenericLabel("Power");
					tmplabel.setHeight(20).setX(140).setY(40);
					MainBox.addWidget(tmplabel);
					faction_tmplabels.addWidget(tmplabel);

					faction_power = new GenericLabel("" + faction.getPowerRounded());
					faction_power.setHeight(20).setX(140).setY(50);
					MainBox.addWidget(faction_power);

					tmplabel = new GenericLabel("Maxpower");
					tmplabel.setHeight(20).setX(190).setY(40);
					MainBox.addWidget(tmplabel);
					faction_tmplabels.addWidget(tmplabel);

					faction_maxpower = new GenericLabel("" + faction.getPowerMaxRounded());
					faction_maxpower.setHeight(20).setX(190).setY(50);
					MainBox.addWidget(faction_maxpower);

					// Members
					faction_membersbutton = new GenericButton("Mitglieder");
					faction_membersbutton.setHeight(13).setX(90).setY(70).setWidth(70);
					MainBox.addWidget(faction_membersbutton);
					faction_membersbutton.setEnabled(false);

					faction_membersbox = new GenericListWidget();
					faction_membersbox.setY(85).setX(90);
					MainBox.addWidget(faction_membersbox);
					faction_membersbox.setHeight(splayer.getMainScreen().getHeight() - faction_membersbox.getY() - 26).setWidth(splayer.getMainScreen().getWidth() - faction_membersbox.getX() - 5);

					for (FPlayer member : faction.getFPlayers()) {
						ListWidgetItem labname = new ListWidgetItem(member.getNameAndTitle(), "Power: " + member.getPowerRounded() + "/" + member.getPowerMaxRounded());
						faction_membersbox.addItem(labname);
					}

					faction_membersbox.setVisible(true);

					// Allianzen
					faction_alliesbutton = new GenericButton("Allianzen");
					faction_alliesbutton.setColor(blue).setHeight(13).setX(170).setY(70).setWidth(70);
					MainBox.addWidget(faction_alliesbutton);

					faction_alliesbox = new GenericListWidget();
					faction_alliesbox.setY(85).setX(90);
					MainBox.addWidget(faction_alliesbox);
					faction_alliesbox.setHeight(splayer.getMainScreen().getHeight() - faction_alliesbox.getY() - 26).setWidth(splayer.getMainScreen().getWidth() - faction_alliesbox.getX() - 5);

					// Einträge Feinde / Allianzen
					for (Faction otherFaction : Factions.i.get()) {
						if (otherFaction.getRelationTo(faction).isAlly()) {
							ListWidgetItem labname = new ListWidgetItem(otherFaction.getTag(), otherFaction.getDescription());
							faction_alliesbox.addItem(labname);
						}
					}
					faction_alliesbox.setVisible(false);

					// Feinde
					faction_enemiesbutton = new GenericButton("Feinde");
					faction_enemiesbutton.setColor(red).setHeight(13).setX(250).setY(70).setWidth(70);
					MainBox.addWidget(faction_enemiesbutton);

					// If Beginners Protection is true, disable Enemies Button
					if (faction.getBeginnerProtection()) {
						faction_enemiesbutton.setEnabled(false);

						float time = (float) (faction.getBeginnerProtectionTime() - System.currentTimeMillis()) / 1000 / 60 / 60;
						DecimalFormat df = new DecimalFormat("0.00");

						faction_enemiesbutton.setTooltip("Anfängerschutz: noch " + df.format(time) + " Stunde(n)");
					} else {
						faction_enemiesbutton.setEnabled(true);
						faction_enemiesbutton.setTooltip(null);
					}

					faction_enemiesbox = new GenericListWidget();
					faction_enemiesbox.setY(85).setX(90);
					MainBox.addWidget(faction_enemiesbox);
					faction_enemiesbox.setHeight(splayer.getMainScreen().getHeight() - faction_enemiesbox.getY() - 26).setWidth(splayer.getMainScreen().getWidth() - faction_enemiesbox.getX() - 5);

					// Einträge Feinde / Allianzen
					for (Faction otherFaction : Factions.i.get()) {
						if (otherFaction.getRelationTo(faction).isEnemy()) {
							ListWidgetItem labname = new ListWidgetItem(otherFaction.getTag(), otherFaction.getDescription());
							faction_enemiesbox.addItem(labname);
						}
					}
					faction_enemiesbox.setVisible(false);

					// Beitreten Button
					if (fplayer.hasFaction() == false) {
						faction_join = new GenericButton("Beitreten");
						if (!(faction.getOpen() || faction.isInvited(fplayer) || fplayer.isAdminBypassing())) {
							faction_join.setEnabled(false).setTooltip("Du bist nicht eingeladen!");
						}
						faction_join.setWidth(70).setY(splayer.getMainScreen().getHeight() - 40).setHeight(15).setX(splayer.getMainScreen().getWidth() - 160);
						MainBox.addWidget(faction_join);
						if (Conf.econEnabled && Conf.econCostJoin != 0 && faction_join.getTooltip() == null)
							faction_join.setTooltip(Conf.econCostJoin + "$");
					} else {
						if (fplayer.getFaction() == faction) {
							faction_leave = new GenericButton("Verlassen");
							faction_leave.setWidth(70).setY(splayer.getMainScreen().getHeight() - 40).setHeight(15).setX(splayer.getMainScreen().getWidth() - 160);
							MainBox.addWidget(faction_leave);
							if (Conf.econEnabled && Conf.econCostLeave != 0)
								faction_leave.setTooltip(Conf.econCostLeave + "$");
						}
					}

					// Faction Flagge
					int x = 0, y = 0;
					for (int type : faction.flag) {
						GenericTexture actexture = new GenericTexture();
						actexture.setUrl("http://dl.dropbox.com/u/14933646/Server/color_" + type + ".png").setWidth(15).setHeight(15).setX(280 + x * 16).setY(y * 16);
						MainBox.addWidget(actexture);
						faction_flag.add(actexture);

						y++;
						if (y > 1) {
							y = 0;
							x++;
						}
					}

				}
			}
		}
	}

	// Chat Menü ################################
	public GenericLabel chat_button;
	public int chat_state;

	public void enableChatMenu(boolean enable) {
		if (chat_button == null) {
			// Chatbutton
			chat_button = new GenericLabel();
			chat_button.setVisible(false);
			splayer.getMainScreen().attachWidget(P.p, chat_button);
		}

		if (enable) {
			chat_button.setVisible(true).setWidth(70).setHeight(15).setY(splayer.getMainScreen().getChatBar().getY() - 50).setX(splayer.getMainScreen().getChatBar().getX());
			chat_state = fplayer.getChatMode().value;

			switch (chat_state) {
			case 0:
				chat_button.setText("Public").setTextColor(white);
			case 1:
				chat_button.setText("Alliance").setTextColor(red);
			case 2:
				chat_button.setText("Faction").setTextColor(green);
			}
		} else {
			chat_button.setVisible(false);
		}
	}

	public void changeChatMenu() {
		chat_state++;
		if (chat_state > 2)
			chat_state = 0;
		switch (chat_state) {
		case 0:
			chat_button.setText("Public").setTextColor(white);
		case 1:
			chat_button.setText("Alliance").setTextColor(red);
		case 2:
			chat_button.setText("Faction").setTextColor(green);
		}
	}

}
