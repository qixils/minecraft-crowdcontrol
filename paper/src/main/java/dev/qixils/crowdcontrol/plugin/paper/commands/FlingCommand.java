package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.RegionalCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static dev.qixils.crowdcontrol.TimedEffect.isActive;

@Getter
public class FlingCommand extends RegionalCommand {
	private final @NotNull String effectName = "fling";

	public FlingCommand(@NotNull PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	private static Vector randomVector() {
		double[] vector = CommandConstants.randomFlingVector();
		return new Vector(vector[0], vector[1], vector[2]);
	}

	@Override
	protected Response.@Nullable Builder precheck(@NotNull Request request) {
		if (isActive("walk", request) || isActive("look", request))
			return request.buildResponse().type(ResultType.RETRY).message("Cannot fling while frozen");
		return null;
	}

	@Override
	protected boolean executeRegionally(Player player, Request request) {
		if (player.isInsideVehicle()) return false;

		player.setVelocity(randomVector());
		return true;
	}

	@Override
	protected Response.@NotNull Builder buildFailure(@NotNull Request request) {
		return request.buildResponse().type(ResultType.RETRY).message("Cannot fling while inside vehicle");
	}
}
