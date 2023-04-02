package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class RespawnCommand extends ImmediateCommand {
	private final String effectName = "respawn";

	public RespawnCommand(FabricCrowdControlPlugin plugin) {
		super(plugin);
	}

	private void teleport(ServerPlayerEntity player, ServerWorld level, BlockPos pos, float angle) {
		sync(() -> player.teleport(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, angle, 0));
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayerEntity> players, @NotNull Request request) {
		Response.Builder response = request.buildResponse().type(Response.ResultType.FAILURE).message("Could not find a respawn point");
		for (ServerPlayerEntity player : players) {
			ServerWorld level = player.server.getWorld(player.getSpawnPointDimension());
			BlockPos pos = player.getSpawnPointPosition();
			float angle = player.getSpawnAngle();
			if (level == null || pos == null) {
				level = player.server.getWorld(World.OVERWORLD);
				if (level == null)
					continue;
				pos = level.getSpawnPos();
			}
			teleport(player, level, pos, angle);
			response.type(Response.ResultType.SUCCESS).message("SUCCESS");
		}
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
