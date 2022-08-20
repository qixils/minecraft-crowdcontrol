package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class FlingCommand extends ImmediateCommand {
	private final @NotNull String displayName = "Fling Randomly";
	private final @NotNull String effectName = "fling";

	public FlingCommand(@NotNull PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	private static Vector randomVector() {
		double[] vector = CommandConstants.randomFlingVector();
		return new Vector(vector[0], vector[1], vector[2]);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		for (Player player : players)
			sync(() -> player.setVelocity(randomVector()));
		return request.buildResponse().type(ResultType.SUCCESS);
	}
}
