package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import com.flowpowered.math.vector.Vector3d;
import dev.qixils.crowdcontrol.common.ExecuteUsing;
import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;

import java.util.List;

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
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		if (isActive("walk", request) || isActive("look", request))
			return request.buildResponse().type(ResultType.RETRY).message("Cannot fling while frozen");

		boolean success = false;
		for (Player player : players) {
			if (player.get(Keys.VEHICLE).orElse(null) != null) continue;

			player.setVelocity(randomVector());
			success = true;
		}

		return success
			? request.buildResponse().type(ResultType.SUCCESS)
			: request.buildResponse().type(ResultType.FAILURE).message("Cannot fling while inside vehicle");
	}
}
