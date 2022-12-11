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
public class DamageCommand<P> implements ImmediateCommand<P> {
	private final @NotNull String effectName = "damage";
	private final @NotNull Plugin<P, ?> plugin;

	@Override
	public @NotNull Component getProcessedDisplayName(@NotNull Request request) {
		if (request.getParameters() == null)
			return getDefaultDisplayName();
		int amount = (int) request.getParameters()[0];
		return getDefaultDisplayName().args(Component.text(amount));
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull P> players, @NotNull Request request) {
		if (request.getParameters() == null)
			return request.buildResponse().type(Response.ResultType.UNAVAILABLE).message("CC is improperly configured and failing to send parameters");
		boolean success = false;
		double amount = (double) request.getParameters()[0] * 2;

		for (P rawPlayer : players) {
			CCPlayer player = plugin.getPlayer(rawPlayer);
			double oldHealth = player.health();
			double newHealth = Math.max(1, oldHealth - amount);
			double appliedDamage = oldHealth - newHealth;
			// don't apply effect unless it is 100% utilized
			if (appliedDamage == amount) {
				success = true;
				sync(() -> player.damage(appliedDamage));
			}
		}

		if (success)
			return request.buildResponse()
					.type(Response.ResultType.SUCCESS);
		else
			return request.buildResponse()
					.type(Response.ResultType.RETRY)
					.message("Players would have been killed by this command");
	}
}

