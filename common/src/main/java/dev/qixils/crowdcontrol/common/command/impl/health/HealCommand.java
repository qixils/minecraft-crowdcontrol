package dev.qixils.crowdcontrol.common.command.impl.health;

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

@RequiredArgsConstructor
@Getter
public class HealCommand<P> implements ImmediateCommand<P> {
	private final @NotNull QuantityStyle quantityStyle = QuantityStyle.APPEND;
	private final @NotNull String effectName = "heal";
	private final @NotNull Plugin<P, ?> plugin;

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull P> players, @NotNull Request request) {
		int amount = request.getQuantityOrDefault() * 2;
		Response.Builder result = request.buildResponse().type(Response.ResultType.RETRY).message("All players are at (or near) full health");

		for (P rawPlayer : players) {
			CCPlayer player = plugin.getPlayer(rawPlayer);
			double oldHealth = player.health();
			double maxHealth = player.maxHealth();
			double newHealth = Math.min(maxHealth, oldHealth + amount);
			// don't apply effect unless it is 100% utilized
			if ((newHealth - oldHealth) == amount) {
				result.type(Response.ResultType.SUCCESS).message("SUCCESS");
				sync(() -> player.health(newHealth));
			}
		}
		return result;
	}
}
