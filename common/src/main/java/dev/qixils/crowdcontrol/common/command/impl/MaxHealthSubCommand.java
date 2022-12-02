package dev.qixils.crowdcontrol.common.command.impl;

import dev.qixils.crowdcontrol.common.Plugin;
import dev.qixils.crowdcontrol.common.command.ImmediateCommand;
import dev.qixils.crowdcontrol.common.mc.CCPlayer;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.MIN_MAX_HEALTH;

@Getter
@RequiredArgsConstructor
public class MaxHealthSubCommand<P> implements ImmediateCommand<P> {
	private final @NotNull String effectName = "max_health_sub";
	private final @NotNull Plugin<P, ?> plugin;

	@Override
	public @NotNull Component getProcessedDisplayName(@NotNull Request request) {
		if (request.getParameters() == null)
			return getDefaultDisplayName();
		return getDefaultDisplayName().args(Component.text((int) request.getParameters()[0]));
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull P> players, @NotNull Request request) {
		if (request.getParameters() == null)
			return request.buildResponse().type(Response.ResultType.UNAVAILABLE).message("CC is improperly configured and failing to send parameters");

		Response.Builder result = request.buildResponse()
				.type(Response.ResultType.FAILURE)
				.message("All players are at minimum health (" + (MIN_MAX_HEALTH / 2) + " hearts)");
		double amount = (double) request.getParameters()[0];

		for (P rawPlayer : players) {
			CCPlayer player = plugin.getPlayer(rawPlayer);
			double current = player.maxHealthOffset();
			double newVal = Math.max(-MIN_MAX_HEALTH, current - amount);
			if (current != newVal) {
				result.type(Response.ResultType.SUCCESS).message("SUCCESS");
				player.maxHealthOffset(newVal);
			}
		}

		return result;
	}
}
