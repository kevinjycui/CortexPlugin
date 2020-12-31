package com.junferno.fear;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.junferno.fear.commands.SetBrainPlayerCommand;
import com.junferno.fear.emotiv.CortexHandler;
import com.junferno.fear.listeners.SpawnListener;
import com.junferno.fear.runnables.BrainRunnable;

public class FearPlugin extends JavaPlugin {

	private static Player brainPlayer = null;
	private static final double RANGE = 50D;

	protected CortexHandler cortex;

	public static boolean setBrainPlayer(Player p) {
		if (p == null || p.isOnline()) {
			brainPlayer = p;
			return true;
		}
		return false;
	}

	public static Player getBrainPlayer() {
		return brainPlayer;
	}

	public static boolean isBrainPlayer(Player p) {
		return p.equals(getBrainPlayer());
	}

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(new SpawnListener(), this);
		new SetBrainPlayerCommand(this);
		
		if (this.cortex == null)
			this.cortex = new CortexHandler();
		else {
			try {
				this.cortex.open();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("FearPlugin - Log check");

		new BrainRunnable(this.cortex).runTaskTimer(this, 0L, 20L * 5);
	}
	
	@Override
	public void onDisable() {
		try {
			this.cortex.close();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
