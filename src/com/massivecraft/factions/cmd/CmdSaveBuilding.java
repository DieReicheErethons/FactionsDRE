package com.massivecraft.factions.cmd;

import com.massivecraft.factions.buildings.BuildingFile;
import com.massivecraft.factions.struct.Permission;

public class CmdSaveBuilding extends FCommand {
	public CmdSaveBuilding() {
		super();
		this.aliases.add("savebuild");
		this.aliases.add("sb");

		this.optionalArgs.put("path", "tmp.building");

		this.disableOnLock = true;
		this.permission = Permission.SAVEBUILDING.node;

		senderMustBePlayer = false;
		senderMustBeMember = false;
		senderMustBeModerator = false;
		senderMustBeAdmin = false;
	}

	@Override
	public void perform() {
		String path = this.argAsString(0);

		if (path == null)
			path = "tmp.building";

		if (fme.getIsclaimabuilding() == null || fme.getFirstclaimblock() == null || fme.getSecondclaimblock() == null) {
			fme.setIsclaimabuilding(path);
			fme.msg("Du protectest ein Gebäude, klicke mit einer Goldschaufel auf den ersten Punkt");

			fme.setFirstclaimblock(null);
			fme.setSecondclaimblock(null);
		} else {
			BuildingFile bfile = BuildingFile.loadFromTwoBlocks(fme.getFirstclaimblock(), fme.getSecondclaimblock());
			bfile.save("plugins/FactionsDRE/buildings/" + fme.getIsclaimabuilding());
			fme.msg("Das Gebäude wurde gespeichert:");
			fme.msg("plugins/FactionsDRE/buildings/" + fme.getIsclaimabuilding());

			fme.setIsclaimabuilding(null);
			fme.setFirstclaimblock(null);
		}
	}
}
