package com.massivecraft.factions.cmd;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FWar;
import com.massivecraft.factions.struct.Permission;

public class CmdInventory extends FCommand {
	public CmdInventory() {
		super();
		this.aliases.add("inventory");

		this.permission = Permission.MOD.node;
		this.disableOnLock = true;

		senderMustBePlayer = true;
		senderMustBeMember = true;
		senderMustBeModerator = true;
		senderMustBeAdmin = true;
	}

	@Override
	public void perform() {
		if (Conf.fwarEnabled) {
			Inventory inv = Bukkit.createInventory(me, 54, "Faction " + fme.getFaction().getTag());
			for (String matString : fme.getFaction().factionInventory.keySet()) {
				MaterialData mat = FWar.convertStringToMaterialData(matString);

				Integer args = fme.getFaction().factionInventory.get(matString);
				int amount = args;

				while (amount > 0) {
					int tmpAmount = mat.toItemStack().getMaxStackSize();
					if (mat.toItemStack().getMaxStackSize() > amount) {
						tmpAmount = amount;
					}
					amount = amount - mat.toItemStack().getMaxStackSize();

					ItemStack item = new ItemStack(mat.getItemType(), tmpAmount, mat.getData());
					inv.addItem(item);
				}
			}

			InventoryView view = me.openInventory(inv);
			fme.playerInventoryView = view;
		}
	}
}
