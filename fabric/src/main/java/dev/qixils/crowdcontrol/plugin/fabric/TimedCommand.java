package dev.qixils.crowdcontrol.plugin.fabric;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public abstract class TimedCommand extends Command implements dev.qixils.crowdcontrol.common.TimedCommand<ServerPlayerEntity> {
	protected TimedCommand(@NotNull FabricCrowdControlPlugin plugin) {
		super(plugin);
	}
}
