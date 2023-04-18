package dev.qixils.crowdcontrol.common.command.impl;

import dev.qixils.crowdcontrol.common.Plugin;
import dev.qixils.crowdcontrol.common.command.ImmediateCommand;
import dev.qixils.crowdcontrol.common.mc.CCPlayer;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.HALVE_HEALTH_MIN_HEALTH;

@Getter
@RequiredArgsConstructor
public class HalfHealthCommand<P> implements ImmediateCommand<P> {
	private final String effectName = "half_health";
	private final Plugin<P, ?> plugin;

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull P> players, @NotNull Request request) {
		Response.Builder response = request.buildResponse()
				.type(ResultType.RETRY)
				.message("Health is already minimum");

		for (P rawPlayer : players) {
			CCPlayer player = plugin.getPlayer(rawPlayer);
			double health = player.health();
			if (health > HALVE_HEALTH_MIN_HEALTH) {
				response.type(ResultType.SUCCESS).message("SUCCESS");
				sync(rawPlayer, () -> player.damage(health / 2f));
			}
		}

		return response;
	}
}
