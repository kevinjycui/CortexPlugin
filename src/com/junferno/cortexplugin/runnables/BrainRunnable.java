package com.junferno.cortexplugin.runnables;

import java.util.HashMap;
import java.util.List;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;

import com.junferno.cortexplugin.CortexPlugin;
import com.junferno.cortexplugin.emotiv.CortexHandler;

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
			this.metrics.put(metricName, 0.5);
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

	protected CortexHandler cortex;
	protected Metric metrics;
	
	private static final double RANGE = 50.0;

	public BrainRunnable(CortexHandler cortex) {
		super();
		this.cortex = cortex;
		this.metrics = new Metric();
	}

	public void modifyAttribute(AttributeInstance attribute, double amount) {
		attribute.setBaseValue(Math.max(0.1, amount));
	}

	public void modifyAttributeByMetric(AttributeInstance attribute, double metric) {
		modifyAttribute(attribute, attribute.getBaseValue() + metric - 0.5);
	}

	public void modifyAttributeBy2Metrics(AttributeInstance attribute, double metric1, double metric2) {
		modifyAttribute(attribute, attribute.getBaseValue() + metric1 + metric2 - 1);
	}

	@Override
	public void run(){
		
		Player p = CortexPlugin.getBrainPlayer();

		if (p == null || this.cortex.getResponse() == null || !p.isOnline())
			return;

		if (!this.metrics.update((double) this.cortex.getResponse().get("time"), (JSONArray) this.cortex.getResponse().get("met")))
			return;

		modifyAttributeBy2Metrics(p.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED), this.metrics.getMetric("exc"), this.metrics.getMetric("lex"));
		modifyAttributeByMetric(p.getAttribute(Attribute.GENERIC_MAX_HEALTH), this.metrics.getMetric("rel"));
		
		for (String met: this.metrics.metrics.keySet())
			p.sendMessage(this.metrics.getMetricMessage(p.getDisplayName(), met));
		List<Entity> entities = p.getLocation().getWorld().getEntities();
		for (Entity entity:entities)
			if (entity.getLocation().distance(p.getLocation()) <= BrainRunnable.RANGE && entity instanceof Monster) {
				modifyAttributeBy2Metrics(((LivingEntity) entity).getAttribute(Attribute.GENERIC_MOVEMENT_SPEED), 
						this.metrics.getMetric("str"), this.metrics.getMetricInverse("rel"));
				modifyAttributeBy2Metrics(((LivingEntity) entity).getAttribute(Attribute.GENERIC_FOLLOW_RANGE), 
						this.metrics.getMetric("str"), this.metrics.getMetricInverse("rel"));
				modifyAttributeBy2Metrics(((LivingEntity) entity).getAttribute(Attribute.GENERIC_ATTACK_DAMAGE), 
						this.metrics.getMetric("str"), this.metrics.getMetricInverse("rel"));
				modifyAttributeByMetric(((LivingEntity) entity).getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE), 
						this.metrics.getMetricInverse("foc"));
			}

	}

}

