package com.junferno.cortex.commands;

import org.bukkit.entity.Player;

import com.junferno.cortex.CortexPlugin;

public class OpenCortexCommand extends CortexCommand {
	
	public OpenCortexCommand(CortexPlugin plugin) {
		plugin.getCommand("open").setExecutor(this);
	}
	
	public boolean checkPerms(Player p) {
		return p.hasPermission("cortex.connection.open");
	}

	public boolean execute(Player p) {
		try {
			if (CortexPlugin.openCortex()) {
				p.sendMessage("Cortex opened successfully");
				return true;
			}
			else {
				p.sendMessage("Cortex already opened");
				return false;
			}
		} catch (InterruptedException e) {
			p.sendMessage("Cortex was interrupted and failed to reconnect");
			return false;
		}
	}

}
