package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.qixils.crowdcontrol.TimedEffect.isActive;

@Getter
public class RespawnCommand extends ImmediateCommand {
	private final String effectName = "respawn";

	public RespawnCommand(ModdedCrowdControlPlugin plugin) {
		super(plugin);
	}

	private void teleport(ServerPlayer player, ServerLevel level, BlockPos pos, float angle) {
		sync(() -> player.teleportTo(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, angle, 0));
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		if (isActive("walk", request) || isActive("look", request))
			return request.buildResponse().type(Response.ResultType.RETRY).message("Cannot fling while frozen");
		Response.Builder response = request.buildResponse().type(Response.ResultType.FAILURE).message("Could not find a respawn point");
		for (ServerPlayer player : players) {
			ServerLevel level = player.server.getLevel(player.getRespawnDimension());
			BlockPos pos = player.getRespawnPosition();
			float angle = player.getRespawnAngle();
			if (level == null || pos == null) {
				level = player.server.getLevel(Level.OVERWORLD);
				if (level == null)
					continue;
				pos = level.getSharedSpawnPos();
			}
			teleport(player, level, pos, angle);
			response.type(Response.ResultType.SUCCESS).message("SUCCESS");
		}
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
