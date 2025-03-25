package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.util.CompletableFutureUtils;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.paper.PaperCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.CHAOS_LOCAL_RADIUS;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.ENTITY_CHAOS_MIN;

@Getter
public class EntityChaosCommand extends PaperCommand {
	private static final int R = CHAOS_LOCAL_RADIUS;
	private final String effectName = "entity_chaos";

	public EntityChaosCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull Player>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(() -> CompletableFuture.<List<CompletableFuture<Boolean>>>supplyAsync(() -> {
			List<Player> players = playerSupplier.get();
			Set<Entity> entities = new HashSet<>();
			if (players.stream().anyMatch(plugin::globalEffectsUsableFor)) {
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
				return Collections.emptyList();

			int i = 0;
			List<CompletableFuture<Boolean>> successes = new ArrayList<>();
			for (Entity entity : entities) {
				CompletableFuture<Boolean> success = new CompletableFuture<>();
				int player = (i++) % players.size();
				entity.getScheduler().run(plugin.getPaperPlugin(), $ -> {
					entity.getPassengers().forEach(entity::removePassenger);
					entity.teleportAsync((players.get(player)).getLocation()).handle((result, error) -> success.complete(error == null && result));
				}, () -> success.complete(false));
				successes.add(success);
			}

			return successes;
		}, plugin.getSyncExecutor()).thenCompose(successes -> successes.isEmpty()
			? CompletableFuture.completedFuture(new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Not enough entities found to teleport"))
			: CompletableFutureUtils.allOf(successes)
			.thenApply($ -> successes.stream().anyMatch(future -> future.state() == Future.State.SUCCESS && future.resultNow())
				? new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS)
				: new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Could not teleport entities"))).join()));
	}
}
