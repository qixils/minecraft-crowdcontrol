package dev.qixils.crowdcontrol.common.command.impl.health;

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

@RequiredArgsConstructor
@Getter
public class HealCommand<P> implements ImmediateCommand<P> {
	private final @NotNull String effectName = "heal";
	private final @NotNull Plugin<P, ?> plugin;

	@Override
	public @NotNull Component getProcessedDisplayName(@NotNull Request request) {
		if (request.getParameters() == null)
			return getDefaultDisplayName();
		int amount = (int) request.getParameters()[0];
		return getDefaultDisplayName().args(Component.text(amount));
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull P> players, @NotNull Request request) {
		if (request.getParameters() == null)
			return request.buildResponse().type(Response.ResultType.UNAVAILABLE).message("CC is improperly configured and failing to send parameters");
		Response.Builder result = request.buildResponse().type(Response.ResultType.FAILURE).message("All players are at (or near) full health");
		double amount = (double) request.getParameters()[0] * 2;

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
