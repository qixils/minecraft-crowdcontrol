package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.common.ExecuteUsing;
import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.math.vector.Vector3d;

import java.util.List;

import static dev.qixils.crowdcontrol.TimedEffect.isActive;

@Getter
@ExecuteUsing(ExecuteUsing.Type.SYNC_GLOBAL)
public class FlingCommand extends ImmediateCommand {
	private final @NotNull String effectName = "fling";

	public FlingCommand(@NotNull SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	private static Vector3d randomVector() {
		double[] vector = CommandConstants.randomFlingVector();
		return new Vector3d(vector[0], vector[1], vector[2]);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		if (isActive("walk", request) || isActive("look", request))
			return request.buildResponse().type(ResultType.RETRY).message("Cannot fling while frozen");

		boolean success = false;
		for (ServerPlayer player : players) {
			if (player.get(Keys.VEHICLE).orElse(null) != null) continue;

			player.offer(Keys.VELOCITY, randomVector());
			success = true;
		}

		return success
			? request.buildResponse().type(ResultType.SUCCESS)
			: request.buildResponse().type(ResultType.FAILURE).message("Cannot fling while inside vehicle");
	}
}
