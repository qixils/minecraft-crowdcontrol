package dev.qixils.crowdcontrol.plugin.fabric;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public abstract class VoidCommand extends Command implements dev.qixils.crowdcontrol.common.VoidCommand<ServerPlayer> {
	protected VoidCommand(@NotNull FabricCrowdControlPlugin plugin) {
		super(plugin);
	}
}
