package dev.qixils.crowdcontrol.plugin.sponge8;

import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.List;
import java.util.Objects;
import java.util.Random;

import static dev.qixils.crowdcontrol.exceptions.ExceptionUtil.validateNotNullElseGet;

public abstract class Command implements dev.qixils.crowdcontrol.common.Command<ServerPlayer> {
	protected static final Random random = RandomUtil.RNG;
	@Getter
	protected final SpongeCrowdControlPlugin plugin;

	protected Command(@NotNull SpongeCrowdControlPlugin plugin) {
		this.plugin = Objects.requireNonNull(plugin, "plugin");
	}

	@Override
	public boolean isClientAvailable(@Nullable List<ServerPlayer> possiblePlayers, @NotNull Request request) {
		if (!plugin.getGame().isClientAvailable())
			return false;
		final List<ServerPlayer> players = validateNotNullElseGet(possiblePlayers, () -> plugin.getPlayers(request));
		if (players.size() != 1)
			return false;
		return plugin.getGame().client().player()
				.map(player -> player.uniqueId().equals(players.get(0).uniqueId()))
				.orElse(false);
	}
}
