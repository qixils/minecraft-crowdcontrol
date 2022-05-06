package dev.qixils.crowdcontrol.plugin.mojmap;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public abstract class TimedCommand extends Command implements dev.qixils.crowdcontrol.common.TimedCommand<ServerPlayer> {
	protected TimedCommand(@NotNull MojmapPlugin<?> plugin) {
		super(plugin);
	}
}
