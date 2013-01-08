package com.massivecraft.factions.buildings;

public class BigBlacksmith extends BuildingType{
	
	public BigBlacksmith(){
		this.name="Grosse Schmiede";
		this.imagepath="http://dl.dropbox.com/u/14933646/Server/blacksmith_1_3.png";
		this.maxtypes=2;
		
		BuildingTypes.add(this);
		
		this.LoadBuildingFiles();
	}
	
	public void LoadBuildingFiles(){
		buildingfile=new BuildingFile[6];
		
		//Normale Schmiede
		buildingfile[0]=BuildingFile.load(buildingpath+"blacksmith/norm_3.building");
		//Orientalische Schmiede
		buildingfile[1]=BuildingFile.load(buildingpath+"blacksmith/orient_3.building");
	}
	
	public void LoadNpcs(){
		
	}
	
	
}
