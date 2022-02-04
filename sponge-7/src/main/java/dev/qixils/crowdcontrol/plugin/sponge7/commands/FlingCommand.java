package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import com.flowpowered.math.vector.Vector3d;
import dev.qixils.crowdcontrol.common.CommandConstants;
import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;

import java.util.List;

@Getter
public class FlingCommand extends ImmediateCommand {
	private final @NotNull String displayName = "Fling Randomly";
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
		for (Player player : players)
			sync(() -> player.setVelocity(randomVector()));
		return request.buildResponse().type(ResultType.SUCCESS);
	}
}
