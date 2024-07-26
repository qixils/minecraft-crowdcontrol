package dev.qixils.crowdcontrol.plugin.sponge8;

import dev.qixils.crowdcontrol.common.util.RandomUtil;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Objects;
import java.util.Random;

public abstract class Command implements dev.qixils.crowdcontrol.common.command.Command<ServerPlayer> {
	protected static final Random random = RandomUtil.RNG;
	@Getter
	protected final @NotNull SpongeCrowdControlPlugin plugin;

	protected Command(@NotNull SpongeCrowdControlPlugin plugin) {
		this.plugin = Objects.requireNonNull(plugin, "plugin");
	}
}
