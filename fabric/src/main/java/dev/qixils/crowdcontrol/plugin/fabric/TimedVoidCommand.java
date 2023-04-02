package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.common.command.TimedCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public abstract class TimedVoidCommand extends VoidCommand implements TimedCommand<ServerPlayerEntity> {
	protected TimedVoidCommand(@NotNull FabricCrowdControlPlugin plugin) {
		super(plugin);
	}
}
