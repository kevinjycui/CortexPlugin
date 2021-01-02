package com.junferno.cortex.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.junferno.cortex.CortexPlugin;

public class SetBrainPlayerCommand implements CommandExecutor {
		
	public SetBrainPlayerCommand(CortexPlugin plugin) {
		plugin.getCommand("brainplayer").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Only players may execute this command!");
			return false;
		}
		
		Player p = (Player) sender;
		
		if (!p.hasPermission("cortex.brainplayer")) {
			p.sendMessage("You do not have permission to execute this command!");
			return false;
		}
		
		else if (args.length > 1) {
			p.sendMessage("Invalid number of arguments");
			return false;
		}
		
		else if (args.length == 0) {
			if (CortexPlugin.setBrainPlayer(p)) {
				p.sendMessage("Brain player set to " + p.getDisplayName());
				return true;
			}
			p.sendMessage("Failed to set brain player to " + p.getDisplayName());
			return false;
		}
		
		for (Player player:p.getWorld().getPlayers()) {
			if (player.getPlayerListName().equals(args[0])) {
				if (!player.isOnline()) {
					p.sendMessage("Cannot set brain player to an offline player");
					return false;
				}
				else if (CortexPlugin.setBrainPlayer(player)) {
					p.sendMessage("Brain player set to " + player.getDisplayName());
					return true;
				}
				p.sendMessage("Failed to set brain player to " + player.getDisplayName());
				return false;
			}
		}
		
		return false;
	}

}
