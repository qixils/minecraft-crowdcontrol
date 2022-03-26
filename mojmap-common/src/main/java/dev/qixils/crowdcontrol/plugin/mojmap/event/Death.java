package dev.qixils.crowdcontrol.plugin.mojmap.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import net.minecraft.world.entity.Entity;

// TODO: impl
@Data
@AllArgsConstructor
@Accessors(fluent = true)
public final class Death implements Event {
	private final Entity entity;
	private boolean keepInventory;
}
