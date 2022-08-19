package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.qixils.crowdcontrol.common.CommandConstants.HALVE_HEALTH_MIN_HEALTH;

@Getter
public class HalfHealthCommand extends ImmediateCommand {
	private final String effectName = "half_health";
	private final String displayName = "Half Health";

	public HalfHealthCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder resp = request.buildResponse()
				.type(ResultType.FAILURE)
				.message("Health is already minimum");

		for (Player player : players) {
			double health = player.getHealth();
			if (health > HALVE_HEALTH_MIN_HEALTH) {
				resp.type(ResultType.SUCCESS).message("SUCCESS");
				sync(() -> {
					player.damage(.1);
					player.setHealth(health / 2);
				});
			}
		}

		return resp;
	}
}
