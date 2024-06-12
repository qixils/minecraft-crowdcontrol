package dev.qixils.crowdcontrol.plugin.paper;

import dev.qixils.crowdcontrol.common.util.CompletableFutureUtils;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class RegionalCommand extends Command {
	protected RegionalCommand(@NotNull PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	protected Response.@Nullable Builder precheck(@NotNull Request request) {
		return null;
	}

	protected abstract boolean executeRegionally(@NotNull Player player, @NotNull Request request);

	protected abstract Response.@NotNull Builder buildFailure(@NotNull Request request);

	@Override
	public @NotNull CompletableFuture<Response.@Nullable Builder> execute(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder precheck = precheck(request);
		if (precheck != null) return CompletableFuture.completedFuture(precheck);

		List<CompletableFuture<Boolean>> futures = new ArrayList<>();
		for (Player player : players) {
			CompletableFuture<Boolean> future = new CompletableFuture<>();
			futures.add(future);
			player.getScheduler().run(plugin, $ -> future.complete(executeRegionally(player, request)), () -> future.complete(false));
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
