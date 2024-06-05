package dev.qixils.crowdcontrol.plugin.paper.mc;

import dev.qixils.crowdcontrol.common.mc.CCLivingEntity;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import static dev.qixils.crowdcontrol.plugin.paper.utils.AttributeUtil.addModifier;

public class PaperLivingEntity extends PaperEntity implements CCLivingEntity {

	public PaperLivingEntity(LivingEntity entity) {
		super(entity);
	}

	@Override
	@NotNull
	public LivingEntity entity() {
		return (LivingEntity) super.entity();
	}

	@Override
	public double health() {
		return entity().getHealth();
	}

	@Override
	public void health(double health) {
		entity().setHealth(health);
	}

	@Override
	public double maxHealth() {
		AttributeInstance attribute = entity().getAttribute(Attribute.GENERIC_MAX_HEALTH);
		if (attribute == null)
			return 20;
		return attribute.getValue();
	}

	@Override
	public double maxHealthOffset() {
		AttributeInstance attribute = entity().getAttribute(Attribute.GENERIC_MAX_HEALTH);
		if (attribute == null)
			return 0;

		AttributeModifier modifier = null;
		for (AttributeModifier attributeModifier : attribute.getModifiers()) {
			if (attributeModifier.getUniqueId() == MAX_HEALTH_MODIFIER_UUID || attributeModifier.getName().equals(MAX_HEALTH_MODIFIER_NAME)) {
				modifier = attributeModifier;
				break;
			}
		}

		return modifier == null ? 0 : modifier.getAmount();
	}

	@Override
	public void maxHealthOffset(double newOffset) {
		addModifier(entity(), Attribute.GENERIC_MAX_HEALTH, MAX_HEALTH_MODIFIER_UUID, MAX_HEALTH_MODIFIER_NAME, newOffset, AttributeModifier.Operation.ADD_NUMBER, true);

		float computedMaxHealth = (float) (20 + newOffset);
		health(Math.min(health(), computedMaxHealth));
	}

	@Override
	public void damage(double damage) {
		entity().damage(damage);
	}

	@Override
	public void heal(double amount) {
		health(health() + amount);
	}

	@Override
	public void kill() {
		health(0);
	}
}
