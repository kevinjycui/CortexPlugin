package com.junferno.cortexplugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.junferno.cortexplugin.CortexPlugin;

public abstract class CortexCommand implements CommandExecutor {
		
	public abstract boolean execute(Player p);

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Only players may execute this command!");
			return false;
		}
		
		Player p = (Player) sender;
		
		if (!p.hasPermission("cortexplugin.admin")) {
			p.sendMessage("You do not have permission to execute this command!");
			return false;
		}
		
		else if (args.length != 1) {
			p.sendMessage("Invalid number of arguments");
			return false;
		}
		
		else if (!args[0].equals("cortex")) {
			p.sendMessage("Invalid argument " + args[0]);
			return false;
		}
		
		return this.execute(p);
		
	}

}
