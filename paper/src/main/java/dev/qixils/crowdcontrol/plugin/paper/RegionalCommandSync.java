package dev.qixils.crowdcontrol.plugin.paper;

import dev.qixils.crowdcontrol.socket.Request;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public abstract class RegionalCommandSync extends RegionalCommand {

	protected RegionalCommandSync(@NotNull PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	protected CompletableFuture<Boolean> executeRegionallyAsync(@NotNull Player player, @NotNull Request request) {
		return CompletableFuture.completedFuture(executeRegionallySync(player, request));
	}

	protected abstract boolean executeRegionallySync(@NotNull Player player, @NotNull Request request);
}
