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

	protected abstract boolean executeRegionally(Player player, Request request);

	protected abstract Response.Builder buildFailure(Request request);

	@Override
	public @NotNull CompletableFuture<Response.@Nullable Builder> execute(@NotNull List<@NotNull Player> players, @NotNull Request request) {
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
			return request.buildResponse().type(Response.ResultType.RETRY).message("Could not find location to place block");
		});
	}
}
