package dev.qixils.crowdcontrol.plugin.sponge8;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public abstract class VoidCommand extends Command implements dev.qixils.crowdcontrol.common.command.VoidCommand<ServerPlayer> {
	protected VoidCommand(@NotNull SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}
}
