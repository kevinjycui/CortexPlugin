package com.junferno.fear.runnables;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;

import com.junferno.fear.FearPlugin;
import com.junferno.fear.emotiv.CortexHandler;

import net.minecraft.server.v1_16_R3.AttributeModifiable;
import net.minecraft.server.v1_16_R3.AttributeModifier;
import net.minecraft.server.v1_16_R3.EntityInsentient;
import net.minecraft.server.v1_16_R3.GenericAttributes;

class Metric {
	protected HashMap<String, Double> metrics = new HashMap<String, Double>();
	private HashMap<Integer, String> metricNames = new HashMap<Integer, String>();
	private HashMap<String, String> metricFullNames = new HashMap<String, String>();

	private JSONArray result;
	private double time;

	public Metric() {
		this.metricNames.put(1, "eng");
		this.metricNames.put(3, "exc");
		this.metricNames.put(4, "lex");
		this.metricNames.put(6, "str");
		this.metricNames.put(8, "rel");
		this.metricNames.put(10, "int");
		this.metricNames.put(12, "foc");

		this.metricFullNames.put("eng", "Engagement");
		this.metricFullNames.put("exc", "Excitement");
		this.metricFullNames.put("lex", "Long-term Excitement");
		this.metricFullNames.put("str", "Stress");
		this.metricFullNames.put("rel", "Relax");
		this.metricFullNames.put("int", "Interest");
		this.metricFullNames.put("foc", "Focus");

		for (String metricName: metricNames.values())
			this.metrics.put(metricName, 1D);
	}

	public double getMetric(String met) {
		return this.metrics.get(met);
	}

	public double getMetricInverse(String met) {
		return 1-this.getMetric(met);
	}

	public String getMetricMessage(String user, String met) {
		return user + ": " + this.metricFullNames.get(met) + " at " + this.getMetric(met);
	}

	public void putIfActive(int activeIndex, int metricIndex) {
		if ((boolean) this.result.get(activeIndex))
			this.metrics.put(this.metricNames.get(metricIndex), (double) this.result.get(metricIndex));
	}

	public boolean update(double time, JSONArray result) {
		if (time == this.time)
			return false;
		
		this.result = result;
		this.time = time;
		
		this.putIfActive(0, 1);
		this.putIfActive(2, 3);
		this.putIfActive(2, 4);
		this.putIfActive(5, 6);
		this.putIfActive(7, 8);
		this.putIfActive(9, 10);
		this.putIfActive(11, 12);
		
		return true;
	}
}

public class BrainRunnable extends BukkitRunnable {

	private static final UUID movementSpeedUID = UUID.fromString("206a89dc-ae78-4c4d-b42c-3b31db3f5a7c");
	protected CortexHandler cortex;
	protected Metric metrics;

	public BrainRunnable(CortexHandler cortex) {
		super();
		this.cortex = cortex;
		this.metrics = new Metric();
	}

	public void modifySpeed(LivingEntity entity, double amount) {
		EntityInsentient nmsEntity = (EntityInsentient) ((CraftLivingEntity) entity).getHandle();
		AttributeModifiable attributes = nmsEntity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED);
		AttributeModifier modifier = new AttributeModifier(
				movementSpeedUID, 
				"Brain plugin movement speed multiplier", 
				amount, 
				AttributeModifier.Operation.MULTIPLY_BASE
				);
		attributes.removeModifier(modifier);
		attributes.addModifier(modifier);
	}

	@Override
	public void run(){

		if (FearPlugin.getBrainPlayer() == null || this.cortex.getResponse() == null)
			return;

		if (!this.metrics.update(
				(double) this.cortex.getResponse().get("time"), 
				(JSONArray) this.cortex.getResponse().get("met")
				))
			return;

		Collection<? extends Player> players = Bukkit.getServer().getOnlinePlayers();
		for (Player p:players) {
			if (FearPlugin.isBrainPlayer(p)) {
				for (String met: this.metrics.metrics.keySet())
					p.sendMessage(this.metrics.getMetricMessage(p.getDisplayName(), met));
				List<Entity> entities = p.getLocation().getWorld().getEntities();
				for (Entity entity:entities)
					if (
							entity instanceof LivingEntity && (
									entity.getType() == EntityType.ZOMBIE || 
									entity.getType() == EntityType.SKELETON || 
									entity.getType() == EntityType.CREEPER
									)
							) {
						modifySpeed((LivingEntity) entity, this.metrics.getMetric("str") + this.metrics.getMetric("exc"));
					}
			}
		}
	}

}

