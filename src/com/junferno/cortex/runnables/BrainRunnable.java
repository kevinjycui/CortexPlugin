package com.junferno.cortex.runnables;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;

import com.junferno.cortex.CortexPlugin;
import com.junferno.cortex.emotiv.CortexHandler;

import net.minecraft.server.v1_16_R3.AttributeBase;
import net.minecraft.server.v1_16_R3.AttributeModifiable;
import net.minecraft.server.v1_16_R3.AttributeModifier;
import net.minecraft.server.v1_16_R3.AttributeModifier.Operation;
import net.minecraft.server.v1_16_R3.EntityLiving;
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
	
	private static final double DFACTOR = 1.0; // Dampening factor, recommended value 1.0
	private static final double TFACTOR = -0.5; // Translational factor, recommended value -0.5
	
	UUID movementSpeedUUID = UUID.fromString("a1d86ac4-c932-4f68-926b-6258d34aa591");
	UUID jumpStrengthUUID = UUID.fromString("f6fdd295-bd04-49c7-89b4-c992069934bd");
	UUID knockbackResistanceUUID = UUID.fromString("de1a690c-fb28-4452-9749-42d6f88bff17");
	UUID attackDamageUUID = UUID.fromString("5a8b5e4a-fceb-45ca-ac12-273798a9a19c");
	UUID attackKnockbackUUID = UUID.fromString("00eb654e-11bb-4ce2-acea-f36abdcefeb7");
	UUID followRangeUUID = UUID.fromString("95ff1f89-7b4f-4ac3-a7e7-1082c93c658f");

	public BrainRunnable(CortexHandler cortex) {
		super();
		this.cortex = cortex;
		this.metrics = new Metric();
	}
	
	public void modifyAttribute(LivingEntity entity, AttributeBase attribute, Operation operation, UUID uuid, double amount) {
		EntityLiving nmsEntity = (EntityLiving) ((CraftLivingEntity) entity).getHandle();
		AttributeModifiable attributes = nmsEntity.getAttributeInstance(attribute);
		if (attributes == null) {
			System.out.println("Entity " + entity.getName() + " does not have attribute " + attribute.getName());
			return;
		}
		AttributeModifier modifier = new AttributeModifier(
				uuid,
				"Brain plugin movement speed multiplier", 
				DFACTOR * (amount + TFACTOR), 
				operation
				);
		attributes.removeModifier(modifier);
		attributes.addModifier(modifier);
	}

	@Override
	public void run(){
		
		Player p = CortexPlugin.getBrainPlayer();
		
		if (p == null || this.cortex.getResponse() == null || !p.isOnline())
			return;

		if (!this.metrics.update((double) this.cortex.getResponse().get("time"), (JSONArray) this.cortex.getResponse().get("met")))
			return;

		modifyAttribute(p, GenericAttributes.MOVEMENT_SPEED, // Movement speed changes with excitement
				AttributeModifier.Operation.MULTIPLY_TOTAL, 
				movementSpeedUUID, 
				this.metrics.getMetric("exc"));

		modifyAttribute(p, GenericAttributes.KNOCKBACK_RESISTANCE, // Knockback resistance changes with inverse of relaxation
				AttributeModifier.Operation.MULTIPLY_TOTAL, 
				knockbackResistanceUUID, 
				this.metrics.getMetricInverse("rel"));

		modifyAttribute(p, GenericAttributes.MAX_HEALTH, // Attack damage changes with focus
				AttributeModifier.Operation.MULTIPLY_TOTAL, 
				attackKnockbackUUID, 
				this.metrics.getMetric("rel"));

		modifyAttribute(p, GenericAttributes.ATTACK_DAMAGE, // Attack damage changes with focus
				AttributeModifier.Operation.MULTIPLY_TOTAL, 
				attackDamageUUID, 
				this.metrics.getMetric("foc"));

		modifyAttribute(p, GenericAttributes.ATTACK_SPEED, // Attack damage changes with focus
				AttributeModifier.Operation.MULTIPLY_TOTAL, 
				attackKnockbackUUID, 
				this.metrics.getMetric("foc"));
		
		for (String met: this.metrics.metrics.keySet())
			p.sendMessage(this.metrics.getMetricMessage(p.getDisplayName(), met));
		List<Entity> entities = p.getLocation().getWorld().getEntities();
		for (Entity entity:entities)
			if (entity.getLocation().distance(p.getLocation()) <= RANGE && entity instanceof Monster) {
				modifyAttribute((Monster) entity, 
						GenericAttributes.MOVEMENT_SPEED, // Monster movement speed changes with stress
						AttributeModifier.Operation.MULTIPLY_TOTAL, 
						movementSpeedUUID, 
						this.metrics.getMetric("str"));
				modifyAttribute((Monster) entity, 
						GenericAttributes.FOLLOW_RANGE, // Monster follow range changes with stress
						AttributeModifier.Operation.MULTIPLY_TOTAL, 
						followRangeUUID, 
						this.metrics.getMetric("str"));
			}

	}

}

