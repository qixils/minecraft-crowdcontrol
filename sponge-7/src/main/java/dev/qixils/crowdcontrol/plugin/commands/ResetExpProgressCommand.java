package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;

import java.util.List;

@Getter
public class ResetExpProgressCommand extends ImmediateCommand {
	private final String effectName = "reset_exp_progress";
	private final String displayName = "Reset Experience";

	public ResetExpProgressCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse().type(Response.ResultType.FAILURE).message("No players have XP");
		for (Player player : players) {
			if (player.get(Keys.TOTAL_EXPERIENCE).orElse(0) > 0) {
				result.type(Response.ResultType.SUCCESS).message("SUCCESS");
				sync(() -> player.offer(Keys.TOTAL_EXPERIENCE, 0));
			}
		}
		return result;
	}
}
