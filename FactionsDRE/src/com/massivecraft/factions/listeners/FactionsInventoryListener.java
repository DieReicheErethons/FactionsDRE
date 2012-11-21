package com.massivecraft.factions.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.FWar;
import com.massivecraft.factions.FWars;
import com.massivecraft.factions.P;

public class FactionsInventoryListener implements Listener{
	public P p;
	public FactionsInventoryListener(P p)
	{
		this.p = p;
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onInventoryClose(InventoryCloseEvent event){
		InventoryView inv=event.getView();
		Player player=(Player) event.getPlayer();
		
		if(inv!=null){
			for(FWar fwar:FWars.i.get()){
				if(fwar!=null){
					if(fwar.tempInvs!=null){
						if(fwar.tempInvs.contains(inv)){
							p.log("test");
							fwar.removeTempInventory(inv);
							player.sendMessage(ChatColor.GREEN+"Items hinzugefügt!");
						}
					}
					
					if(fwar.tempInvsFromTarget!=null){
						if(fwar.tempInvsFromTarget.contains(inv)){
							fwar.removeTempInventoryFromTarget(inv);
							player.sendMessage(ChatColor.GREEN+"Items hinzugefügt!");
						}
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onInventoryClick(InventoryClickEvent event){
		InventoryView inv=event.getView();
		Player player=(Player) event.getWhoClicked();
		ItemStack istack=event.getCurrentItem();
		
		/* Faction Inventory */
		for(FPlayer fpl:FPlayers.i.get()){
			if(inv.equals(fpl.playerInventoryView)){
				event.setCancelled(true);
				
				if(event.getRawSlot()<54){
					ItemStack dataStack=istack.clone();
					
					ItemStack lostItems= player.getInventory().addItem(istack).get(0);
					event.setCurrentItem(lostItems);
					
					for(String matString:fpl.getFaction().factionInventory.keySet()){
						MaterialData mat=FWar.convertStringToMaterialData(matString);
						Integer args=fpl.getFaction().factionInventory.get(matString);
						
						if(mat.equals(dataStack.getData())){
							if(lostItems!=null){
								args=args-(dataStack.getAmount()-lostItems.getAmount());
							}else{
								args=args-dataStack.getAmount();
							}
							
							fpl.getFaction().factionInventory.put(FWar.convertMaterialDataToString(mat), args);
						}
					}
				}
			}
		}
		
		/*War Pay-Inventory */
		if(inv!=null){
			for(FWar fwar:FWars.i.get()){
				if(fwar!=null){
					if(fwar.tempInvsFromTarget!=null){
						if(fwar.tempInvsFromTarget.contains(inv)){
							event.setCancelled(true);
							
							if(event.getRawSlot()>54){
								for(ItemStack tempIStack:inv.getTopInventory().getContents()){
									if(tempIStack!=null){
										if(tempIStack.getType()==istack.getType()){
											if(istack.getAmount()>tempIStack.getAmount()){
												istack.setAmount(istack.getAmount()-tempIStack.getAmount());
												tempIStack.setAmount(0);
											} else {
												tempIStack.setAmount(tempIStack.getAmount()-istack.getAmount());
												istack.setAmount(0);
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
}
