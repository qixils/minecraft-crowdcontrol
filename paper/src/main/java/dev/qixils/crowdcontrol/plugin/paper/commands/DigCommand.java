package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.util.CompletableFutureUtils;
import dev.qixils.crowdcontrol.plugin.paper.Command;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.DIG_RADIUS;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.getDigDepth;

@Getter
public class DigCommand extends Command {
	private final String effectName = "dig";

	public DigCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public @NotNull CompletableFuture<Response.Builder> execute(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		List<CompletableFuture<Boolean>> statuses = new ArrayList<>();
		int depth = getDigDepth();
		for (Player player : players) {
			CompletableFuture<Boolean> status = new CompletableFuture<>();
			statuses.add(status);
			Location playerLocation = player.getLocation();
			sync(playerLocation, () -> {
				boolean success = false;
				for (double x = -DIG_RADIUS; x <= DIG_RADIUS; ++x) {
					for (int y = depth; y < 0; ++y) {
						for (double z = -DIG_RADIUS; z <= DIG_RADIUS; ++z) {
							Block block = playerLocation.clone().add(x, y, z).getBlock();
							if (!block.isEmpty()) {
								block.setType(Material.AIR);
								success = true;
							}
						}
					}
				}
				status.complete(success);
			});
		}

		return CompletableFutureUtils.allOf(statuses).thenApplyAsync(successes -> {
			boolean success = successes.stream().anyMatch(Boolean::booleanValue);
			Response.Builder response = request.buildResponse();
			if (!success)
				response.type(Response.ResultType.RETRY).message("Streamer(s) not standing on any blocks");
			else
				response.type(Response.ResultType.SUCCESS);
			return response;
		});
	}
}
