package dev.qixils.crowdcontrol.plugin.fabric;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public abstract class VoidCommand extends Command implements dev.qixils.crowdcontrol.common.command.VoidCommand<ServerPlayer> {
	protected VoidCommand(@NotNull ModdedCrowdControlPlugin plugin) {
		super(plugin);
	}
}
