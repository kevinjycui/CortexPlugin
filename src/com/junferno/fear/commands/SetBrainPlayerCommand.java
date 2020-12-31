package com.junferno.fear.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.junferno.fear.FearPlugin;

public class SetBrainPlayerCommand implements CommandExecutor {
	
	private FearPlugin plugin;
	
	public SetBrainPlayerCommand(FearPlugin plugin) {
		this.plugin = plugin;
		plugin.getCommand("brainplayer").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Only players may execute this command!");
			return false;
		}
		
		Player p = (Player) sender;
		
		if (args.length > 1) {
			p.sendMessage("Invalid number of commands");
			return false;
		}
		
		else if (args.length == 0) {
			if (FearPlugin.setBrainPlayer(p)) {
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
				else if (FearPlugin.setBrainPlayer(player)) {
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
