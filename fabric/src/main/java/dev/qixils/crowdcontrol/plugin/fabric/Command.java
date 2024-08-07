package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.common.util.RandomUtil;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Random;

public abstract class Command implements dev.qixils.crowdcontrol.common.command.Command<ServerPlayer> {
	protected static final @NotNull Random random = RandomUtil.RNG;
	@Getter
	protected final @NotNull FabricCrowdControlPlugin plugin;

	protected Command(@NotNull FabricCrowdControlPlugin plugin) {
		this.plugin = Objects.requireNonNull(plugin, "plugin");
	}
}
