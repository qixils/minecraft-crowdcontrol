package dev.qixils.crowdcontrol.plugin.sponge8;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public abstract class TimedCommand extends Command implements dev.qixils.crowdcontrol.common.TimedCommand<ServerPlayer> {
	protected TimedCommand(@NotNull SpongeCrowdControlPlugin plugin, boolean isEventListener) {
		super(plugin, isEventListener);
	}

	protected TimedCommand(@NotNull SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}
}
