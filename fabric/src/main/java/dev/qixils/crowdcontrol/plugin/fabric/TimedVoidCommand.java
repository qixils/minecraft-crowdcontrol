package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.common.command.TimedCommand;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public abstract class TimedVoidCommand extends VoidCommand implements TimedCommand<ServerPlayer> {
	protected TimedVoidCommand(@NotNull FabricCrowdControlPlugin plugin) {
		super(plugin);
	}
}
