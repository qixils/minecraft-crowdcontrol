package dev.qixils.crowdcontrol.plugin.fabric.event;

import lombok.Data;
import lombok.experimental.Accessors;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;

@Data
@Accessors(fluent = true, chain = false)
public final class Damage implements CancellableEvent {
	private final Entity entity;
	private final DamageSource source;
	private float amount;
	private boolean cancelled = false;

	public Damage(Entity entity, DamageSource source, float amount) {
		this.entity = entity;
		this.source = source;
		this.amount = amount;
	}
}
