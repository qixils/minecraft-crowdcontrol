package dev.qixils.crowdcontrol.plugin.fabric.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true, chain = false)
public final class Damage implements CancellableEvent {
	private final Entity entity;
	private final DamageSource source;
	private final float amount;
	private boolean cancelled = false;
}
