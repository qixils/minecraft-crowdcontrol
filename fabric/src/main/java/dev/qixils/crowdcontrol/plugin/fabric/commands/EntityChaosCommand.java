package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.CHAOS_LOCAL_RADIUS;

@Getter
public class EntityChaosCommand extends ImmediateCommand {
	private static final int R = CHAOS_LOCAL_RADIUS * CHAOS_LOCAL_RADIUS;
	private final String effectName = "entity_chaos";

	public EntityChaosCommand(FabricCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayerEntity> players, @NotNull Request request) {
		sync(() -> {
			Set<Entity> entities = new HashSet<>(200);
			if (isGlobalCommandUsable(players, request)) {
				for (ServerWorld level : plugin.server().getWorlds()) {
					for (Entity entity : level.iterateEntities()) {
						if (entity.getType() == EntityType.PLAYER) continue;
						entities.add(entity);
					}
				}
			} else {
				for (ServerPlayerEntity player : players) {
					Vec3d pp = player.getPos();
					for (Entity entity : player.getWorld().iterateEntities()) {
						if (entity.getType() == EntityType.PLAYER) continue;
						Vec3d ep = entity.getPos();
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
				entity.removeAllPassengers();
				Vec3d dest = players.get((i++) % players.size()).getPos();
				entity.requestTeleport(dest.x, dest.y, dest.z);
			}
		});
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
