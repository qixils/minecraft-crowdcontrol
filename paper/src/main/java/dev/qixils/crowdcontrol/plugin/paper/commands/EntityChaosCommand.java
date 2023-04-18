package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.CHAOS_LOCAL_RADIUS;

@Getter
public class EntityChaosCommand extends ImmediateCommand {
	private static final int R = CHAOS_LOCAL_RADIUS;
	private final String effectName = "entity_chaos";

	public EntityChaosCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		sync(() -> {
			Set<Entity> entities = new HashSet<>(200);
			if (isGlobalCommandUsable(players, request)) {
				for (World world : Bukkit.getWorlds()) {
					for (Entity entity : world.getEntities()) {
						if (entity.getType() == EntityType.PLAYER) continue;
						entities.add(entity);
					}
				}
			} else {
				for (Player player : players) {
					for (Entity entity : player.getNearbyEntities(R, R, R)) {
						if (entity.getType() == EntityType.PLAYER) continue;
						entities.add(entity);
					}
				}
			}
			int i = 0;
			for (Entity entity : entities) {
				entity.getPassengers().forEach(entity::removePassenger);
				entity.teleportAsync((players.get((i++) % players.size())).getLocation());
			}
		});
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
