package dev.qixils.crowdcontrol.plugin.fabric.mc;

import dev.qixils.crowdcontrol.common.mc.CCLivingEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

import static dev.qixils.crowdcontrol.plugin.fabric.utils.AttributeUtil.addModifier;
import static net.minecraft.resources.ResourceLocation.withDefaultNamespace;

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
		AttributeInstance attribute = entity().getAttribute(Attributes.MAX_HEALTH);
		if (attribute == null) {
			return 0;
		}
		AttributeModifier modifier = attribute.getModifier(withDefaultNamespace(MAX_HEALTH_MODIFIER_UUID.toString().toLowerCase(Locale.US)));
		return modifier == null ? 0 : modifier.amount();
	}

	@Override
	public void maxHealthOffset(double newOffset) {
		addModifier(entity(), Attributes.MAX_HEALTH, MAX_HEALTH_MODIFIER_UUID, newOffset, AttributeModifier.Operation.ADD_VALUE, true);

		float computedMaxHealth = (float) (20 + newOffset);
		health(Math.min(health(), computedMaxHealth));
	}

	@Override
	public void damage(double damage) {
		entity().hurt(entity().damageSources().generic(), (float) damage);
	}

	@Override
	public void heal(double amount) {
		entity().heal((float) amount);
	}
}
