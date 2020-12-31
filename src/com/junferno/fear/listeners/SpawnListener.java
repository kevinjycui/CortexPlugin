package com.junferno.fear.listeners;

import java.util.Collection;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.junferno.fear.FearPlugin;

public class SpawnListener implements Listener {

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		if (FearPlugin.getBrainPlayer() == null && FearPlugin.setBrainPlayer(event.getPlayer()))
			System.out.println("Brain player set to " + event.getPlayer().getDisplayName());
	}

	@EventHandler
	public void onLeave(PlayerQuitEvent event) {
		Player p = event.getPlayer();
		if (p.equals(FearPlugin.getBrainPlayer())) {
			Collection<? extends Player> players = event.getPlayer().getServer().getOnlinePlayers();
			for (Player q:players)
				if (!p.equals(q)) {
					FearPlugin.setBrainPlayer(q);
					p.sendMessage("Brain player " + p.getDisplayName() + " quit the game, brain player set to " + q.getDisplayName());
					System.out.println("Brain player " + p.getDisplayName() + " quit the game, brain player set to " + q.getDisplayName());
					return;
				}
			FearPlugin.setBrainPlayer(null);
			p.sendMessage("Brain player " + p.getDisplayName() + " quit the game, there is no more brain player");
			System.out.println("Brain player " + p.getDisplayName() + " quit the game, there is no more brain player");
		}
	}

}
