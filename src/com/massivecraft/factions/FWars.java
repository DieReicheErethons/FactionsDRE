package com.massivecraft.factions;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.inventory.InventoryView;

import com.google.gson.reflect.TypeToken;
import com.massivecraft.factions.zcore.persist.EntityCollection;

public class FWars extends EntityCollection<FWar>{

	public static FWars i = new FWars();

	P p = P.p;

	private FWars()
	{
		super
		(
			FWar.class,
			new CopyOnWriteArrayList<FWar>(),
			new ConcurrentHashMap<String, FWar>(),
			new File(P.p.getDataFolder(), "wars.json"),
			P.p.gson
		);
	}

	@Override
	public Type getMapType() {
		return new TypeToken<Map<String, FWar>>(){}.getType();
	}

	@Override
	public boolean loadFromDisc()
	{
		if ( ! super.loadFromDisc()) return false;

		for(FWar war:this.get()){
			war.tempInvs=new HashSet<InventoryView>();
			war.tempInvsFromTarget=new HashSet<InventoryView>();
		}

		FWar.checkWars();

		return true;

	}
}
