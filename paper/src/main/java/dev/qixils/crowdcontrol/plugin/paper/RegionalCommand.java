package dev.qixils.crowdcontrol.plugin.paper;

import dev.qixils.crowdcontrol.common.util.CompletableFutureUtils;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static net.kyori.adventure.text.Component.text;

public abstract class RegionalCommand extends Command {
	protected RegionalCommand(@NotNull PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	protected @Nullable CCEffectResponse precheck(@NotNull List<@NotNull Player> players, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		return null;
	}

	protected abstract CompletableFuture<Boolean> executeRegionallyAsync(@NotNull Player player, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer);

	protected abstract @NotNull CCEffectResponse buildFailure(@NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer);

	private CompletableFuture<Boolean> executeSafely(@NotNull Player player, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		return executeRegionallyAsync(player, request, ccPlayer).handle((success, error) -> {
			if (success) return true;
			if (error != null) getPlugin().getSLF4JLogger().error(text("Failed to execute ").append(getDisplayName()), error);
			return false;
		});
	}

	protected int getPlayerLimit() {
		return 0;
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull Player>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ThreadUtil.waitForSuccess(() -> {
			List<Player> players = playerSupplier.get();
			CCEffectResponse precheck = precheck(players, request, ccPlayer);
			if (precheck != null) return precheck;

			int playerLimit = getPlayerLimit();
			if (playerLimit > 0) {
				boolean hostsBypass = plugin.getLimitConfig().hostsBypass();
				AtomicInteger victims = new AtomicInteger();
				players = players.stream()
					.sorted(Comparator.comparing(plugin::globalEffectsUsableFor))
					.takeWhile(player -> victims.getAndAdd(1) < playerLimit || (hostsBypass && plugin.globalEffectsUsableFor(player)))
					.toList();
			}

			List<CompletableFuture<Boolean>> futures = new ArrayList<>();
			for (Player player : players) {
				CompletableFuture<Boolean> future = new CompletableFuture<>();
				futures.add(future);
				player.getScheduler().run(plugin.getPaperPlugin(), $ -> executeSafely(player, request, ccPlayer).thenApply(future::complete), () -> future.complete(false));
			}

			return CompletableFutureUtils.allOf(futures).handleAsync(($1, $2) -> {
				for (CompletableFuture<Boolean> future : futures) {
					try {
						if (future.get())
							return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS);
					} catch (Exception ignored) {}
				}
				return buildFailure(request, ccPlayer);
			}).join();
		});
	}
}
