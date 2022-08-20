package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public abstract class Command implements dev.qixils.crowdcontrol.common.command.Command<ServerPlayer> {
	protected static final Random random = RandomUtil.RNG;
	@Getter
	protected final FabricCrowdControlPlugin plugin;

	protected Command(@NotNull FabricCrowdControlPlugin plugin) {
		this.plugin = Objects.requireNonNull(plugin, "plugin");
	}

	@Override
	public boolean isClientAvailable(@Nullable List<ServerPlayer> possiblePlayers, @NotNull Request request) {
		return plugin.isClientAvailable(possiblePlayers, request);
	}
}
