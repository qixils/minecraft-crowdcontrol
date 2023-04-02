package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.common.command.TimedCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public abstract class TimedImmediateCommand extends ImmediateCommand implements TimedCommand<ServerPlayerEntity> {
	protected TimedImmediateCommand(@NotNull FabricCrowdControlPlugin plugin) {
		super(plugin);
	}
}
