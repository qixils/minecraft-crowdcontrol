package dev.qixils.crowdcontrol.plugin.fabric.mc;

import dev.qixils.crowdcontrol.common.mc.CCLivingEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import org.jetbrains.annotations.NotNull;

public class FabricLivingEntity extends FabricEntity implements CCLivingEntity {

	public FabricLivingEntity(LivingEntity entity) {
		super(entity);
	}

	@Override
	public @NotNull LivingEntity entity() {
		return (LivingEntity) super.entity();
	}

	@Override
	public double health() {
		return entity().getHealth();
	}

	@Override
	public void health(double health) {
		entity().setHealth((float) health);
	}

	@Override
	public double maxHealth() {
		return entity().getMaxHealth();
	}

	@Override
	public double maxHealthOffset() {
		EntityAttributeInstance attribute = entity().getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
		if (attribute == null) {
			return 0;
		}
		EntityAttributeModifier modifier = null;
		for (EntityAttributeModifier attributeModifier : attribute.getModifiers()) {
			if (attributeModifier.getId() == MAX_HEALTH_MODIFIER_UUID || attributeModifier.getName().equals(MAX_HEALTH_MODIFIER_NAME)) {
				modifier = attributeModifier;
				break;
			}
		}
		return modifier == null ? 0 : modifier.getValue();
	}

	@Override
	public void maxHealthOffset(double newOffset) {
		EntityAttributeInstance maxHealthAttr = entity().getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
		if (maxHealthAttr == null) {
			logger.warn("Player missing GENERIC_MAX_HEALTH attribute");
			return;
		}
		for (EntityAttributeModifier attributeModifier : maxHealthAttr.getModifiers()) {
			if (attributeModifier.getId() == MAX_HEALTH_MODIFIER_UUID || attributeModifier.getName().equals(MAX_HEALTH_MODIFIER_NAME)) {
				maxHealthAttr.removeModifier(attributeModifier);
			}
		}
		maxHealthAttr.addPersistentModifier(new EntityAttributeModifier(
				MAX_HEALTH_MODIFIER_UUID,
				MAX_HEALTH_MODIFIER_NAME,
				newOffset,
				EntityAttributeModifier.Operation.ADDITION
		));
		float computedMaxHealth = (float) (20 + newOffset);
		health(Math.min(health(), computedMaxHealth));
	}

	@Override
	public void damage(double damage) {
		entity().damageWithModifier(entity().getDamageSources().outOfWorld(), (float) damage);
	}

	@Override
	public void heal(double amount) {
		entity().heal((float) amount);
	}
}
