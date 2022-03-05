package dev.qixils.crowdcontrol.plugin.fabric;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public abstract class VoidCommand extends Command implements dev.qixils.crowdcontrol.common.VoidCommand<ServerPlayerEntity> {
	protected VoidCommand(@NotNull FabricCrowdControlPlugin plugin) {
		super(plugin);
	}
}
