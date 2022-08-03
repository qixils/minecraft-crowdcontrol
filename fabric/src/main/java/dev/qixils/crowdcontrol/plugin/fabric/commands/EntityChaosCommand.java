package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static dev.qixils.crowdcontrol.common.CommandConstants.CHAOS_LOCAL_RADIUS;

@Getter
public class EntityChaosCommand extends ImmediateCommand {
	private static final int R = CHAOS_LOCAL_RADIUS * CHAOS_LOCAL_RADIUS;
	private final String displayName = "Entity Chaos";
	private final String effectName = "entity_chaos";

	public EntityChaosCommand(FabricCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		sync(() -> {
			Set<Entity> entities = new HashSet<>(200);
			if (isGlobalCommandUsable(players, request)) {
				for (ServerLevel level : plugin.server().getAllLevels()) {
					for (Entity entity : level.getAllEntities()) {
						if (entity.getType() == EntityType.PLAYER) continue;
						entities.add(entity);
					}
				}
			} else {
				// TODO test this
				for (ServerPlayer player : players) {
					Vec3 pp = player.position();
					for (Entity entity : player.getLevel().getAllEntities()) {
						if (entity.getType() == EntityType.PLAYER) continue;
						Vec3 ep = entity.position();
						double x = pp.x - ep.x;
						double y = pp.y - ep.y;
						double z = pp.z - ep.z;
						if (x * x + y * y + z * z < R)
							entities.add(entity);
					}
				}
			}
			int i = 0;
			for (Entity entity : entities) {
				entity.ejectPassengers();
				Vec3 dest = players.get((i++) % players.size()).position();
				entity.teleportTo(dest.x, dest.y, dest.z);
			}
		});
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
