package com.junferno.cortexplugin;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.junferno.cortexplugin.commands.SetBrainPlayerCommand;
import com.junferno.cortexplugin.emotiv.CortexHandler;
import com.junferno.cortexplugin.listeners.SpawnListener;
import com.junferno.cortexplugin.runnables.BrainRunnable;

public class CortexPlugin extends JavaPlugin {

	private static Player brainPlayer = null;
	private static final double RANGE = 50D;

	private static CortexHandler cortex;

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
	
	public static boolean openCortex() throws InterruptedException {
		return CortexPlugin.cortex.open();
	}
	
	public static boolean closeCortex() throws InterruptedException {
		return CortexPlugin.cortex.close();
	}

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(new SpawnListener(), this);
		new SetBrainPlayerCommand(this);
		
		if (CortexPlugin.cortex == null)
			CortexPlugin.cortex = new CortexHandler();
		else {
			try {
				CortexPlugin.cortex.open();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		new BrainRunnable(CortexPlugin.cortex).runTaskTimer(this, 0L, 20L * 5);
	}
	
	@Override
	public void onDisable() {
		try {
			CortexPlugin.cortex.close();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
