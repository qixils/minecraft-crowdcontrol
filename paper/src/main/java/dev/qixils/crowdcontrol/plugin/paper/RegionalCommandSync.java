package dev.qixils.crowdcontrol.plugin.paper;

import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public abstract class RegionalCommandSync extends RegionalCommand {

	protected RegionalCommandSync(@NotNull PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	protected CompletableFuture<Boolean> executeRegionallyAsync(@NotNull Player player, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		return CompletableFuture.completedFuture(executeRegionallySync(player, request, ccPlayer));
	}

	protected abstract boolean executeRegionallySync(@NotNull Player player, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer);
}
