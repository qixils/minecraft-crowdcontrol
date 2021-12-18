package dev.qixils.crowdcontrol.plugin;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;

public abstract class TimedCommand extends Command implements dev.qixils.crowdcontrol.common.TimedCommand<Player> {
	protected TimedCommand(@NotNull SpongeCrowdControlPlugin plugin, boolean isEventListener) {
		super(plugin, isEventListener);
	}

	protected TimedCommand(@NotNull SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}
}
