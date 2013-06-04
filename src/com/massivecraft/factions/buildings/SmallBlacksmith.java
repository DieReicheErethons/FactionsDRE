package com.massivecraft.factions.buildings;

import com.massivecraft.factions.P;

public class SmallBlacksmith extends BuildingType {

	public SmallBlacksmith() {
		this.name = "Kleine Schmiede";
		this.imagepath = "http://dl.dropbox.com/u/14933646/Server/blacksmith_1_1.png";
		this.maxtypes = 2;
		this.nextBuilding = MEDIUMBLACKSMITH;

		BuildingTypes.add(this);

		P.p.log(this.name + " inizialisiert");

		this.LoadBuildingFiles();
	}

	public void LoadBuildingFiles() {
		buildingfile = new BuildingFile[6];

		// Normale Schmiede
		buildingfile[0] = BuildingFile.load(buildingpath + "blacksmith/norm_1.building");
		// Orientalische Schmiede
		buildingfile[1] = BuildingFile.load(buildingpath + "blacksmith/orient_1.building");
	}

	public void LoadNpcs() {

	}

}
