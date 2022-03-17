package dev.qixils.crowdcontrol.plugin.fabric;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public abstract class ImmediateCommand extends Command implements dev.qixils.crowdcontrol.common.ImmediateCommand<ServerPlayer> {
	protected ImmediateCommand(@NotNull FabricCrowdControlPlugin plugin) {
		super(plugin);
	}
}
