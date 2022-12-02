package dev.qixils.crowdcontrol.common.command.impl;

import dev.qixils.crowdcontrol.common.Plugin;
import dev.qixils.crowdcontrol.common.command.ImmediateCommand;
import dev.qixils.crowdcontrol.common.mc.CCPlayer;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class FullHealCommand<P> implements ImmediateCommand<P> {
	private final @NotNull String effectName = "full_heal";
	private final @NotNull Plugin<P, ?> plugin;

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull P> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse().type(Response.ResultType.FAILURE).message("All players are at maximum health");
		for (P rawPlayer : players) {
			CCPlayer player = plugin.getPlayer(rawPlayer);
			double maxHealth = player.maxHealth();
			if (player.health() < maxHealth) {
				result.type(Response.ResultType.SUCCESS).message("SUCCESS");
				sync(() -> player.health(maxHealth));
			}
		}
		return result;
	}
}
