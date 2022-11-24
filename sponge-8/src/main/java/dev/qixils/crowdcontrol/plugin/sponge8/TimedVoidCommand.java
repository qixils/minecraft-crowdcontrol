package dev.qixils.crowdcontrol.plugin.sponge8;

import dev.qixils.crowdcontrol.common.command.TimedCommand;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public abstract class TimedVoidCommand extends VoidCommand implements TimedCommand<ServerPlayer> {
	protected TimedVoidCommand(@NotNull SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}
}
