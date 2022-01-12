package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class ResetExpProgressCommand extends ImmediateCommand {
	private final String effectName = "reset_exp_progress";
	private final String displayName = "Reset Experience Progress";

	public ResetExpProgressCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse().type(Response.ResultType.FAILURE).message("No players have XP");
		for (Player player : players) {
			if (player.getTotalExperience() > 0) {
				result.type(Response.ResultType.SUCCESS).message("SUCCESS");
				sync(() -> player.setTotalExperience(0));
			}
		}
		return result;
	}
}
