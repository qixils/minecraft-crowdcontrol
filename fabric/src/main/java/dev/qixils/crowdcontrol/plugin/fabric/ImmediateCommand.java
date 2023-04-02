package dev.qixils.crowdcontrol.plugin.fabric;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public abstract class ImmediateCommand extends Command implements dev.qixils.crowdcontrol.common.command.ImmediateCommand<ServerPlayerEntity> {
	protected ImmediateCommand(@NotNull FabricCrowdControlPlugin plugin) {
		super(plugin);
	}
}
