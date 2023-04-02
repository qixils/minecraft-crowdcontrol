package dev.qixils.crowdcontrol.plugin.fabric.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;


@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true, chain = false)
public final class Jump implements CancellableEvent {
	private final @NotNull PlayerEntity player;
	private final boolean isClientSide;
	private boolean cancelled;
}
