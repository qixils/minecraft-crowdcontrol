package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.plugin.fabric.client.FabricPlatformClient;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Random;

import static dev.qixils.crowdcontrol.exceptions.ExceptionUtil.validateNotNullElseGet;

public abstract class Command implements dev.qixils.crowdcontrol.mojmap.Command {
	protected static final Random random = RandomUtil.RNG;
	@Getter
	protected final FabricCrowdControlPlugin plugin;

	protected Command(@NotNull FabricCrowdControlPlugin plugin) {
		this.plugin = Objects.requireNonNull(plugin, "plugin");
	}

	@Override
	public boolean isClientAvailable(@Nullable List<ServerPlayer> possiblePlayers, @NotNull Request request) {
		if (!FabricCrowdControlPlugin.CLIENT_INITIALIZED)
			return false;
		final List<ServerPlayer> players = validateNotNullElseGet(possiblePlayers, () -> plugin.getPlayers(request));
		if (players.size() != 1)
			return false;
		return FabricPlatformClient.get().player()
				.map(player -> player.getUUID().equals(players.get(0).getUUID()))
				.orElse(false);
	}
}
