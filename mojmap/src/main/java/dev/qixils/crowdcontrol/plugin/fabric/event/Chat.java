package dev.qixils.crowdcontrol.plugin.fabric.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.minecraft.server.level.ServerPlayer;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true, chain = false)
public final class Chat implements CancellableEvent {
	private final ServerPlayer player;
	private final String message;
	private boolean cancelled = false;
}
