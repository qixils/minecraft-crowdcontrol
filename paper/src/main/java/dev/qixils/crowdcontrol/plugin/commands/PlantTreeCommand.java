package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.plugin.BukkitCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.Command;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.Builder;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.bukkit.TreeType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Getter
public class PlantTreeCommand extends Command {
	private final String effectName = "plant_tree";
	private final String displayName = "Plant Tree";

	public PlantTreeCommand(BukkitCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public @NotNull CompletableFuture<Builder> execute(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder resp = request.buildResponse().type(ResultType.FAILURE).message("Streamer is not in a suitable place for tree planting");
		TreeType treeType = RandomUtil.randomElementFrom(TreeType.values());

		List<CompletableFuture<Void>> futures = new ArrayList<>(players.size());
		for (Player player : players) {
			CompletableFuture<Void> future = new CompletableFuture<>();
			futures.add(future);

			sync(() -> {
				if (player.getWorld().generateTree(player.getLocation(), random, treeType))
					resp.type(ResultType.SUCCESS).message("SUCCESS");
				future.complete(null);
			});
		}

		// waits for all trees to get planted, then returns the resulting builder
		return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenApply($ -> resp);
	}
}
