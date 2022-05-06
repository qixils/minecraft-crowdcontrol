package dev.qixils.crowdcontrol.plugin.mojmap;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public abstract class ImmediateCommand extends Command implements dev.qixils.crowdcontrol.common.ImmediateCommand<ServerPlayer> {
	protected ImmediateCommand(@NotNull MojmapPlugin<?> plugin) {
		super(plugin);
	}
}
