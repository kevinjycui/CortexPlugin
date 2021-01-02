package com.junferno.cortexplugin.commands;

import org.bukkit.entity.Player;

import com.junferno.cortexplugin.CortexPlugin;

public class CloseCortexCommand extends CortexCommand {
	
	public CloseCortexCommand(CortexPlugin plugin) {
		plugin.getCommand("close").setExecutor(this);
	}

	public boolean execute(Player p) {
		try {
			if (CortexPlugin.closeCortex()) {
				p.sendMessage("Cortex closed successfully");
				return true;
			}
			else {
				p.sendMessage("Cortex already closed");
				return false;
			}
		} catch (InterruptedException e) {
			p.sendMessage("Cortex was interrupted and failed to disconnect");
			return false;
		}
	}

}
