package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.CHAOS_LOCAL_RADIUS;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.ENTITY_CHAOS_MIN;

@Getter
public class EntityChaosCommand extends ImmediateCommand {
	private final EntityType<Player> PLAYER = EntityTypes.PLAYER.get();
	private final Predicate<Entity> IS_NOT_PLAYER = entity -> !entity.type().equals(PLAYER);
	private final String effectName = "entity_chaos";

	public EntityChaosCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Set<Entity> entities = new HashSet<>(200);
		if (isGlobalCommandUsable(players, request)) {
			for (ServerWorld world : plugin.getGame().server().worldManager().worlds())
				entities.addAll(world.entities().stream().filter(IS_NOT_PLAYER).collect(Collectors.toList()));
		} else {
			for (Player player : players) {
				for (Entity entity : player.nearbyEntities(CHAOS_LOCAL_RADIUS)) {
					if (entity.type().equals(PLAYER)) continue;
					entities.add(entity);
				}
			}
		}

		if (entities.size() < ENTITY_CHAOS_MIN)
			return request.buildResponse()
				.type(Response.ResultType.RETRY)
				.message("Not enough entities found to teleport");

		int i = 0;
		boolean success = false;
		for (Entity entity : entities) {
			entity.offer(Keys.PASSENGERS, Collections.emptyList());
			success |= entity.setLocation(players.get((i++) % players.size()).serverLocation());
		}

		return success
			? request.buildResponse().type(Response.ResultType.SUCCESS)
			: request.buildResponse().type(Response.ResultType.RETRY).message("Could not teleport entities");
	}
}
