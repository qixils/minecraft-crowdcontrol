package dev.qixils.crowdcontrol.plugin.paper;

import dev.qixils.crowdcontrol.common.util.CompletableFutureUtils;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static net.kyori.adventure.text.Component.text;

public abstract class RegionalCommand extends Command {
	protected RegionalCommand(@NotNull PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	protected Response.@Nullable Builder precheck(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		return null;
	}

	protected abstract boolean executeRegionally(@NotNull Player player, @NotNull Request request);

	protected abstract Response.@NotNull Builder buildFailure(@NotNull Request request);

	private boolean executeSafely(@NotNull Player player, @NotNull Request request) {
		try {
			return executeRegionally(player, request);
		} catch (Exception e) {
			getPlugin().getSLF4JLogger().error(text("Failed to execute ").append(getDisplayName()), e);
			return false;
		}
	}

	protected int getPlayerLimit() {
		return 0;
	}

	@Override
	public @NotNull CompletableFuture<Response.@Nullable Builder> execute(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder precheck = precheck(players, request);
		if (precheck != null) return CompletableFuture.completedFuture(precheck);

		int playerLimit = getPlayerLimit();
		if (playerLimit > 0) {
			boolean hostsBypass = plugin.getLimitConfig().hostsBypass();
			AtomicInteger victims = new AtomicInteger();
			players = players.stream()
				.sorted(Comparator.comparing(this::isHost))
				.takeWhile(player -> victims.getAndAdd(1) < playerLimit || (hostsBypass && isHost(player)))
				.toList();
		}

		List<CompletableFuture<Boolean>> futures = new ArrayList<>();
		for (Player player : players) {
			CompletableFuture<Boolean> future = new CompletableFuture<>();
			futures.add(future);
			player.getScheduler().run(plugin, $ -> future.complete(executeSafely(player, request)), () -> future.complete(false));
		}

		return CompletableFutureUtils.allOf(futures).handleAsync(($1, $2) -> {
			for (CompletableFuture<Boolean> future : futures) {
				try {
					if (future.get())
						return request.buildResponse().type(Response.ResultType.SUCCESS);
				} catch (Exception ignored) {}
			}
			return buildFailure(request);
		});
	}
}
