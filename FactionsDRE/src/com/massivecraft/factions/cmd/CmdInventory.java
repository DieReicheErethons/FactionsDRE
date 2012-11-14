package com.massivecraft.factions.cmd;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

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
		for(Material mat:fme.getFaction().factionInventory.keySet()){
			 Integer[] args=fme.getFaction().factionInventory.get(mat);
			 int data = args[0];
			 MaterialData matdata=new MaterialData(data);
			 int amount = args[1];
			 ItemStack item = new ItemStack(mat);
			 while(amount>0){
				
				if(mat.getMaxStackSize()<=amount){
					item.setAmount(mat.getMaxStackSize());
					item.setData(matdata);
					
					amount=amount-mat.getMaxStackSize();
					
				}else{
					item.setAmount(amount);
					item.setData(matdata);
					amount=0;
				}
				inv.addItem(item);
			 }
			 
			 
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
