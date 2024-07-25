package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.ExecuteUsing;
import dev.qixils.crowdcontrol.common.util.CompletableFutureUtils;
import dev.qixils.crowdcontrol.plugin.paper.Command;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.CHAOS_LOCAL_RADIUS;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.ENTITY_CHAOS_MIN;
import static java.util.concurrent.CompletableFuture.completedFuture;

@ExecuteUsing(ExecuteUsing.Type.SYNC_GLOBAL)
@Getter
public class EntityChaosCommand extends Command {
	private static final int R = CHAOS_LOCAL_RADIUS;
	private final String effectName = "entity_chaos";

	public EntityChaosCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public @NotNull CompletableFuture<Response.Builder> execute(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Set<Entity> entities = new HashSet<>();
		if (isGlobalCommandUsable(players, request)) {
			for (World world : Bukkit.getWorlds()) {
				for (Entity entity : world.getEntities()) {
					if (entity.getType() == EntityType.PLAYER) continue;
					entities.add(entity);
				}
			}
		} else {
			for (Player player : players) {
				// TODO: folia ...?
				for (Entity entity : player.getNearbyEntities(R, R, R)) {
					if (entity.getType() == EntityType.PLAYER) continue;
					entities.add(entity);
				}
			}
		}

		if (entities.size() < ENTITY_CHAOS_MIN)
			return completedFuture(request.buildResponse()
				.type(Response.ResultType.RETRY)
				.message("Not enough entities found to teleport"));

		int i = 0;
		List<CompletableFuture<Boolean>> successes = new ArrayList<>();
		for (Entity entity : entities) {
			CompletableFuture<Boolean> success = new CompletableFuture<>();
			int player = (i++) % players.size();
			entity.getScheduler().run(plugin, $ -> {
				entity.getPassengers().forEach(entity::removePassenger);
				entity.teleportAsync((players.get(player)).getLocation()).handle((result, error) -> success.complete(error != null && result));
			}, () -> success.complete(false));
			successes.add(success);
		}

		return CompletableFutureUtils.allOf(successes)
			.thenApply($ -> successes.stream().anyMatch(future -> future.state() == Future.State.SUCCESS && future.resultNow() )
			? request.buildResponse().type(Response.ResultType.SUCCESS)
			: request.buildResponse().type(Response.ResultType.RETRY).message("Could not teleport entities"));
	}
}
