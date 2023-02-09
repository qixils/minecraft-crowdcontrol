package dev.qixils.crowdcontrol.plugin.sponge8.mc;

import dev.qixils.crowdcontrol.common.mc.CCLivingEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSources;

public class SpongeLivingEntity extends SpongeEntity implements CCLivingEntity {

	private static final double BASE_MAX_HEALTH = 20.0;

	public SpongeLivingEntity(Living entity) {
		super(entity);
	}

	@Override
	public @NotNull Living entity() {
		return (Living) super.entity();
	}

	@Override
	public double health() {
		return entity().health().get();
	}

	@Override
	public void health(double health) {
		entity().offer(Keys.HEALTH, health);
	}

	@Override
	public double maxHealth() {
		return entity().maxHealth().get();
	}

	@Override
	public double maxHealthOffset() {
		return maxHealth() - BASE_MAX_HEALTH;
	}

	@Override
	public void maxHealthOffset(double newOffset) {
		entity().offer(Keys.MAX_HEALTH, BASE_MAX_HEALTH + newOffset);
	}

	@Override
	public void damage(double damage) {
		entity().damage(damage, DamageSources.VOID);
	}

	@Override
	public void heal(double amount) {
		health(health() + amount);
	}

	@Override
	public void kill() {
		damage(Integer.MAX_VALUE);
	}
}
