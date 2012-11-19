package com.massivecraft.factions.cmd;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import com.massivecraft.factions.FWar;
import com.massivecraft.factions.struct.Permission;


public class CmdInventory extends FCommand
{
	public CmdInventory()
	{
		super();
		this.aliases.add("inventory");
		
		//this.requiredArgs.add("player name");
		//this.optionalArgs.put("", "");
		
		this.permission = Permission.MOD.node;
		this.disableOnLock = true;
		
		senderMustBePlayer = true;
		senderMustBeMember = true;
		senderMustBeModerator = true;
		senderMustBeAdmin = false;
	}

	@Override
	public void perform() {
		Inventory inv=Bukkit.createInventory(me, 54);
		for(String matString:fme.getFaction().factionInventory.keySet()){
			MaterialData mat=FWar.convertStringToMaterialData(matString);
			 Integer args=fme.getFaction().factionInventory.get(matString);
			 int amount = args;
			 me.sendMessage("Itesmsasdf");
			 //item.setTypeId(mat.getItemTypeId());
			 //item.setData(mat);
			 //while(amount>0){
			 
				ItemStack item = new ItemStack(mat.getItemType(),amount,mat.getData());
				/*if(mat.getItemType().getMaxStackSize()<=amount){
					item.setAmount(mat.getItemType().getMaxStackSize());
					item.setData(mat);
					
					amount=amount-mat.getItemType().getMaxStackSize();
					inv.addItem(item);
					
				}else{
					item.setAmount(amount);
					item.setData(mat);
					amount=0;
					inv.addItem(item);
				}*/
				if(amount>0){
					inv.addItem(item);
				}
				
			 //}
			 
			 
		}
		/*for(ItemStack istack:fme.getFaction().factionInventory){
			if(count<54){
				inv.addItem(istack);
			}
			count++;
		}*/
		InventoryView view=me.openInventory(inv);
		fme.playerInventoryView=view;
		
		
	}
}
