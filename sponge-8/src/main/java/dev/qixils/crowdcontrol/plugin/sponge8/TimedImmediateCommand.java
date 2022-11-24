package dev.qixils.crowdcontrol.plugin.sponge8;

import dev.qixils.crowdcontrol.common.command.TimedCommand;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public abstract class TimedImmediateCommand extends ImmediateCommand implements TimedCommand<ServerPlayer> {
	protected TimedImmediateCommand(@NotNull SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}
}
