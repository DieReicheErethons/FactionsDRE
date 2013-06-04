package com.massivecraft.factions.buildings;

public class MediumBlacksmith extends BuildingType {

	public MediumBlacksmith() {
		this.name = "Mittlere Schmiede";
		this.imagepath = "http://dl.dropbox.com/u/14933646/Server/blacksmith_1_2.png";
		this.maxtypes = 2;

		BuildingTypes.add(this);

		this.LoadBuildingFiles();
	}

	public void LoadBuildingFiles() {
		buildingfile = new BuildingFile[6];

		// Normale Schmiede
		buildingfile[0] = BuildingFile.load(buildingpath + "blacksmith/norm_2.building");
		// Orientalische Schmiede
		buildingfile[1] = BuildingFile.load(buildingpath + "blacksmith/orient_2.building");
	}

	public void LoadNpcs() {

	}

}
