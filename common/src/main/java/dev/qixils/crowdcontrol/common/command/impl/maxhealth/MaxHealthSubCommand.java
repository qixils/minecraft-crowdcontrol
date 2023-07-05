package dev.qixils.crowdcontrol.common.command.impl.maxhealth;

import dev.qixils.crowdcontrol.common.Plugin;
import dev.qixils.crowdcontrol.common.command.ImmediateCommand;
import dev.qixils.crowdcontrol.common.command.QuantityStyle;
import dev.qixils.crowdcontrol.common.mc.CCPlayer;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.MIN_MAX_HEALTH;

@Getter
@RequiredArgsConstructor
public class MaxHealthSubCommand<P> implements ImmediateCommand<P> {
	private final @NotNull QuantityStyle quantityStyle = QuantityStyle.APPEND;
	private final @NotNull String effectName = "max_health_sub";
	private final @NotNull Plugin<P, ?> plugin;

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull P> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(Response.ResultType.FAILURE)
				.message("All players are at minimum health (" + (MIN_MAX_HEALTH / 2) + " hearts)");

		int amount = request.getQuantityOrDefault();

		for (P rawPlayer : players) {
			CCPlayer player = plugin.getPlayer(rawPlayer);
			sync(() -> {
				double current = player.maxHealthOffset();
				double newVal = Math.max(-MIN_MAX_HEALTH, current - amount);
				if ((current - newVal) == amount) {
					result.type(Response.ResultType.SUCCESS).message("SUCCESS");
					player.maxHealthOffset(newVal);
					player.health(Math.min(player.health(), player.maxHealth()));
				}
			});
		}

		return result;
	}
}
