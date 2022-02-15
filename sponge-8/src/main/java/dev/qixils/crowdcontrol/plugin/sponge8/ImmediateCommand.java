package dev.qixils.crowdcontrol.plugin.sponge8;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public abstract class ImmediateCommand extends Command implements dev.qixils.crowdcontrol.common.ImmediateCommand<ServerPlayer> {
	protected ImmediateCommand(@NotNull SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}
}
