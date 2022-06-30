package dev.qixils.crowdcontrol.plugin.mojmap.event;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.minecraft.world.entity.player.Player;

@Data
@RequiredArgsConstructor
@Accessors(fluent = true, chain = false)
public final class Jump implements CancellableEvent {
	private final Player player;
	private boolean cancelled = false;
}
