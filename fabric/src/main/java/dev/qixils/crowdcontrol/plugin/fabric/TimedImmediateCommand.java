package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.common.command.TimedCommand;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public abstract class TimedImmediateCommand extends ImmediateCommand implements TimedCommand<ServerPlayer> {
	protected TimedImmediateCommand(@NotNull FabricCrowdControlPlugin plugin) {
		super(plugin);
	}
}
