package dev.qixils.crowdcontrol.plugin.sponge8;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;

public abstract class VoidCommand extends Command implements dev.qixils.crowdcontrol.common.VoidCommand<Player> {
	protected VoidCommand(@NotNull SpongeCrowdControlPlugin plugin, boolean isEventListener) {
		super(plugin, isEventListener);
	}

	protected VoidCommand(@NotNull SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}
}
