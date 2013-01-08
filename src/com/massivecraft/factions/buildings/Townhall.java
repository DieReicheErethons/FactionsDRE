package com.massivecraft.factions.buildings;

public class Townhall extends BuildingType{
	public Townhall(){
		this.name="Rathaus";
		this.maxtypes=1;
		
		BuildingTypes.add(this);
		
		this.LoadBuildingFiles();
	}
	
	public void LoadBuildingFiles(){
		
	}
	
	public void LoadNpcs(){
		
	}
}
