package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@Getter
@ParametersAreNonnullByDefault
public class MoveCommand extends ImmediateCommand {
	protected final @NotNull Vector vector;
	protected final @NotNull String effectName;
	protected final @NotNull String displayName;

	public MoveCommand(PaperCrowdControlPlugin plugin, Vector displacement, String effectName, String displayName) {
		super(plugin);
		vector = displacement;
		this.effectName = effectName;
		this.displayName = "Fling " + displayName;
	}

	public MoveCommand(PaperCrowdControlPlugin plugin, double x, double y, double z, String effectName, String displayName) {
		this(plugin, new Vector(x, y, z), effectName, displayName);
	}

	public MoveCommand(PaperCrowdControlPlugin plugin, Vector displacement, String effectName) {
		this(plugin, displacement, effectName, effectName);
	}

	public MoveCommand(PaperCrowdControlPlugin plugin, double x, double y, double z, String effectName) {
		this(plugin, x, y, z, effectName, effectName);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(List<@NotNull Player> players, Request request) {
		Response.Builder resp = request.buildResponse().type(ResultType.RETRY).message("All players were grounded");
		boolean isDownwards = vector.getY() < 0.0;
		for (Player player : players) {
			// we are not worried about hackers or desync, just ensuring viewers get what they paid for
			//noinspection deprecation
			if (isDownwards && player.isOnGround())
				continue;
			resp.type(ResultType.SUCCESS).message("SUCCESS");
			sync(() -> player.setVelocity(vector));
		}
		return resp;
	}
}
