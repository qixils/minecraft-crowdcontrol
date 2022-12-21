package dev.qixils.crowdcontrol.common.command.impl.maxhealth;

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

@Getter
@RequiredArgsConstructor
public class MaxHealthAddCommand<P> implements ImmediateCommand<P> {
	private final @NotNull String effectName = "max_health_add";
	private final @NotNull Plugin<P, ?> plugin;

	@Override
	public @NotNull Component getProcessedDisplayName(@NotNull Request request) {
		if (request.getParameters() == null)
			return getDefaultDisplayName();
		return getDefaultDisplayName().args(Component.text((int) (double) request.getParameters()[0]));
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull P> players, @NotNull Request request) {
		if (request.getParameters() == null)
			return request.buildResponse().type(Response.ResultType.UNAVAILABLE).message("CC is improperly configured and failing to send parameters");

		double amount = (double) request.getParameters()[0];

		for (P rawPlayer : players) {
			CCPlayer player = plugin.getPlayer(rawPlayer);
			player.maxHealthOffset(player.maxHealthOffset() + amount);
			player.health(player.health() + amount);
		}

		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
