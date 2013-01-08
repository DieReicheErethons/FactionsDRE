package com.massivecraft.factions.buildings;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.massivecraft.factions.FPlayer;

public class BuildingType {
	//Public BuildingTypes
	public static SmallBlacksmith SMALLBLACKSMITH;
	public static MediumBlacksmith MEDIUMBLACKSMITH;
	public static BigBlacksmith BIGBLACKSMITH;
	public static Townhall TOWNHALL;
	public static Warphouse WARPHOUSE;
	
	//Fields
	public static Set<BuildingType> BuildingTypes=new HashSet<BuildingType>(); 
	
	protected BuildingFile[] buildingfile;
	
	public String name;
	public String imagepath;
	
	protected int maxtypes;
	
	protected String buildingpath="plugins/FactionsDRE/buildings/";
	
	protected BuildingType nextBuilding;
	
	
	
	//Functions
	public static void init(){
		//etc.
		BuildingTypes=new HashSet<BuildingType>(); 
		
		//Buildings
		MEDIUMBLACKSMITH=new MediumBlacksmith();
		SMALLBLACKSMITH=new SmallBlacksmith();
		BIGBLACKSMITH=new BigBlacksmith();
		
	}
	
	public BuildingFile getBuildingFile(int Type){
		if(Type>=this.maxtypes) return null;
		return this.buildingfile[Type-1];
	}
	
	public void checkBuilding(FPlayer me, long pX, long pY, long pZ, int type){
		World world=me.getPlayer().getWorld();
		
		
		BuildingFile bfile=this.getBuildingFile(type);
		
		if (bfile!=null){
			Set<Block> tmpchangedBlock = new HashSet<Block>();
			for(short x=0;x<bfile.width;x++){
				for(short z=0;z<bfile.length;z++){
					for(short y=0;y<bfile.height;y++){
						// Ist es ein Ignorierter Block?
						if(!isIgnoredBlock((byte)bfile.getSeeBlock(x, y, z, 0))){
							Location tmplocation = new Location(world,x+pX-bfile.width/2,pY+y-bfile.nullpoint,z+pZ-bfile.length/2);
							Block block = tmplocation.getBlock();
							tmpchangedBlock.add(block);
							if(!me.getChangedBlock().contains(block)){
								if(y==bfile.nullpoint){
									if(block.getType()==Material.AIR){
										me.getPlayer().sendBlockChange(tmplocation, 35, (byte) 14);
									}else{
										me.getPlayer().sendBlockChange(tmplocation, 35, (byte) 0);
									}
								}else if(y>bfile.nullpoint){
									if(BuildingType.isIgnoredBlock((byte)block.getTypeId())){
										me.getPlayer().sendBlockChange(tmplocation, 35, (byte) 5);
									}else{
										me.getPlayer().sendBlockChange(tmplocation, 35, (byte) 14);
									}
								}else{
									me.getPlayer().sendBlockChange(tmplocation, 35, (byte) 12);
								}
								
							}
						}
					}
				}
			}
			
			//Alte Blockliste löschen und neu füllen
			for(Block block:me.getChangedBlock()){
				if(!tmpchangedBlock.contains(block)){
					me.getPlayer().sendBlockChange(block.getLocation(), block.getTypeId(), block.getData());
				}
			}
			me.setChangedBlock(tmpchangedBlock);
		}
	}
	
	public static boolean isIgnoredBlock(byte block){
		if(
				block==0
				||block==6
				||block==8
				||block==9
				||block==10
				||block==11
				||block==30
				||block==31
				||block==32
				||block==37
				||block==38
				||block==39
				||block==40
				||block==50
				||block==51
				||block==59
				||block==78
				||block==106
				||block==111
				||block==115
			) return true;

		return false;
	}
	
}
