package com.massivecraft.factions.integration.spout;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.getspout.spoutapi.event.input.*;
import org.getspout.spoutapi.gui.ScreenType;
import org.getspout.spoutapi.player.SpoutPlayer;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.buildings.BuildingFile;

public class SpoutInputListener implements Listener{
	private boolean ctrlPressed;	
	public boolean isCtrlPressed() {return ctrlPressed;} 
	public boolean setCtrlPressed(boolean ctrlPressed) {this.ctrlPressed = ctrlPressed;return ctrlPressed;}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onKeyPressedEvent(KeyPressedEvent event) {
		
		String key=event.getKey().toString();
		SpoutPlayer sPlayer=event.getPlayer();
		FPlayer player= FPlayers.i.get(sPlayer.getPlayer());
		
		if(player==null){
			return;
		}
		
		if(player.getsMenu()==null){
			return;
		}
		
		if(key.equals("KEY_LCONTROL")  || key.equals("KEY_RCONTROL"))
			setCtrlPressed(true);
		
		if(key.equals("KEY_X")) {
			if(sPlayer.getActiveScreen()==ScreenType.GAME_SCREEN){
				player.getsMenu().changeMenu(0);
			}else if(sPlayer.getMainScreen().getActivePopup()==player.getsMenu().menuPopup){
				player.getsMenu().menuPopup.close();
	        	sPlayer.openScreen(ScreenType.GAME_SCREEN);
			} 
		}
		
		if(key.equals("KEY_P")) {
			if (isCtrlPressed()){
				if(player.isBuilding()!=null){
					World world=player.getPlayer().getWorld();
					
					BuildingFile bfile=player.isBuilding().getBuildingFile(1);
					
					if (bfile!=null){
						int pX=player.getLastseelocation().getBlockX();
						int pY=player.getLastseelocation().getBlockY();
						int pZ=player.getLastseelocation().getBlockZ();
	
						for(short x=0;x<bfile.width;x++){
							for(short z=0;z<bfile.length;z++){
								for(short y=0;y<bfile.height;y++){
									if (bfile.getBlock(x, y, z, 0)!=0){
										Location tmplocation = new Location(world,x+pX-bfile.width/2,pY+y-bfile.nullpoint,z+pZ-bfile.length/2);
										Block block = tmplocation.getBlock();
										block.setTypeId(bfile.getBlock(x, y, z, 0));
										block.setData((byte) bfile.getBlockData(x, y, z, 0));
									}
								}
							}
						}
					}
					player.setIsBuilding(null);
				}
			}
		}
			
	}
	

	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onKeyReleasedEvent(KeyReleasedEvent event) {
		String key=event.getKey().toString();
		if(key.equals("KEY_LCONTROL")  || event.getKey().equals("KEY_RCONTROL"))
			setCtrlPressed(false);
	}
}
