package dev.qixils.crowdcontrol.plugin;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;

public abstract class ImmediateCommand extends Command implements dev.qixils.crowdcontrol.common.ImmediateCommand<Player> {
	protected ImmediateCommand(@NotNull SpongeCrowdControlPlugin plugin, boolean isEventListener) {
		super(plugin, isEventListener);
	}

	protected ImmediateCommand(@NotNull SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}
}
