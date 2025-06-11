package dev.qixils.crowdcontrol.plugin.paper;

import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static net.kyori.adventure.text.Component.text;

public abstract class RegionalCommand extends PaperCommand {
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
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			List<Player> players = playerSupplier.get();
			CCEffectResponse precheck = precheck(players, request, ccPlayer);
			if (precheck != null) return precheck;

			int playerLimit = getPlayerLimit();

			return executeLimit(request, players, playerLimit, player -> {
				boolean success = false;
				try {
					CompletableFuture<Boolean> future = new CompletableFuture<>();
					player.getScheduler().run(plugin.getPaperPlugin(), $ -> executeSafely(player, request, ccPlayer).thenApply(future::complete), () -> future.complete(false));
					success = future.join();
				} catch (Exception ignored) {
				}
				return success
					? new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS)
					: buildFailure(request, ccPlayer);
			});
		}));
	}
}
