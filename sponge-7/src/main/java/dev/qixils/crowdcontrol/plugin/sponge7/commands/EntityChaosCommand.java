package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.World;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static dev.qixils.crowdcontrol.common.CommandConstants.CHAOS_LOCAL_RADIUS;

@Getter
public class EntityChaosCommand extends ImmediateCommand {
	private static final Predicate<Entity> IS_NOT_PLAYER = entity -> !entity.getType().equals(EntityTypes.PLAYER);
	private final String displayName = "Entity Chaos";
	private final String effectName = "entity_chaos";

	public EntityChaosCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Set<Entity> entities = new HashSet<>(200);
		if (isGlobalCommandUsable(players, request)) {
			for (World world : plugin.getGame().getServer().getWorlds())
				entities.addAll(world.getEntities(IS_NOT_PLAYER));
		} else {
			for (Player player : players) {
				for (Entity entity : player.getNearbyEntities(CHAOS_LOCAL_RADIUS)) {
					if (entity.getType().equals(EntityTypes.PLAYER)) continue;
					entities.add(entity);
				}
			}
		}

		sync(() -> {
			int i = 0;
			for (Entity entity : entities) {
				entity.clearPassengers();
				entity.setLocation(players.get((i++) % players.size()).getLocation());
			}
		});

		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
