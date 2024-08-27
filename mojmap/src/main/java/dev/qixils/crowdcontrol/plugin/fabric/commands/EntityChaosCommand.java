package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.ExecuteUsing;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
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

import static dev.qixils.crowdcontrol.common.command.CommandConstants.CHAOS_LOCAL_RADIUS;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.ENTITY_CHAOS_MIN;
import static java.util.Collections.emptySet;

@ExecuteUsing(ExecuteUsing.Type.SYNC_GLOBAL)
@Getter
public class EntityChaosCommand extends ImmediateCommand {
	private static final int R = CHAOS_LOCAL_RADIUS * CHAOS_LOCAL_RADIUS;
	private final String effectName = "entity_chaos";

	public EntityChaosCommand(ModdedCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Set<Entity> entities = new HashSet<>(200);
		if (isGlobalCommandUsable(players, request)) {
			for (ServerLevel level : plugin.server().getAllLevels()) {
				for (Entity entity : level.getAllEntities()) {
					if (entity.getType() == EntityType.PLAYER) continue;
					entities.add(entity);
				}
			}
		} else {
			for (ServerPlayer player : players) {
				Vec3 pp = player.position();
				for (Entity entity : ((ServerLevel) player.level()).getAllEntities()) {
					// TODO: I can probably reduce some loops here right?
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

		if (entities.size() < ENTITY_CHAOS_MIN)
			return request.buildResponse()
				.type(Response.ResultType.RETRY)
				.message("Not enough entities found to teleport");

		int i = 0;
		boolean success = false;
		for (Entity entity : entities) {
			entity.ejectPassengers();
			ServerPlayer target = players.get((i++) % players.size());
			Vec3 dest = target.position();
			success |= entity.teleportTo(target.serverLevel(), dest.x, dest.y, dest.z, emptySet(), entity.getYRot(), entity.getXRot());
		}

		return success
			? request.buildResponse().type(Response.ResultType.SUCCESS)
			: request.buildResponse().type(Response.ResultType.RETRY).message("Could not teleport entities");
	}
}
