package com.junferno.cortexplugin.listeners;

import java.util.Collection;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.junferno.cortexplugin.CortexPlugin;

public class SpawnListener implements Listener {

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		if (CortexPlugin.getBrainPlayer() == null && CortexPlugin.setBrainPlayer(event.getPlayer()))
			System.out.println("Brain player set to " + event.getPlayer().getDisplayName());
	}

	@EventHandler
	public void onLeave(PlayerQuitEvent event) {
		Player p = event.getPlayer();
		if (p.equals(CortexPlugin.getBrainPlayer())) {
			Collection<? extends Player> players = event.getPlayer().getServer().getOnlinePlayers();
			for (Player q:players)
				if (!p.equals(q)) {
					CortexPlugin.setBrainPlayer(q);
					p.sendMessage("Brain player " + p.getDisplayName() + " quit the game, brain player set to " + q.getDisplayName());
					System.out.println("Brain player " + p.getDisplayName() + " quit the game, brain player set to " + q.getDisplayName());
					return;
				}
			CortexPlugin.setBrainPlayer(null);
			p.sendMessage("Brain player " + p.getDisplayName() + " quit the game, there is no more brain player");
			System.out.println("Brain player " + p.getDisplayName() + " quit the game, there is no more brain player");
		}
	}

}
