package dev.qixils.crowdcontrol.plugin.mojmap.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.minecraft.world.entity.LivingEntity;

// TODO: impl
@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true, chain = false)
public final class Death implements CancellableEvent {
	private final LivingEntity entity;
	private boolean keepInventory = false;
	private boolean cancelled = false;

	public Death(LivingEntity entity, boolean keepInventory) {
		this.entity = entity;
		this.keepInventory = keepInventory;
	}
}
