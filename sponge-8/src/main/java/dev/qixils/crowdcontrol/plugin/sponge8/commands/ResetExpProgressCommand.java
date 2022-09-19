package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.List;

@Getter
public class ResetExpProgressCommand extends ImmediateCommand {
	private final String effectName = "reset_exp_progress";

	public ResetExpProgressCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse().type(Response.ResultType.FAILURE).message("No players have XP");
		for (ServerPlayer player : players) {
			if (player.get(Keys.EXPERIENCE).orElse(0) > 0) {
				result.type(Response.ResultType.SUCCESS).message("SUCCESS");
				sync(() -> player.offer(Keys.EXPERIENCE, 0));
			}
		}
		return result;
	}
}
