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

@Getter
@RequiredArgsConstructor
public class MaxHealthAddCommand<P> implements ImmediateCommand<P> {
	private final @NotNull QuantityStyle quantityStyle = QuantityStyle.APPEND;
	private final @NotNull String effectName = "max_health_add";
	private final @NotNull Plugin<P, ?> plugin;

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull P> players, @NotNull Request request) {
		int amount = request.getQuantityOrDefault();

		for (P rawPlayer : players) {
			CCPlayer player = plugin.getPlayer(rawPlayer);
			player.maxHealthOffset(player.maxHealthOffset() + amount);
			player.health(player.health() + amount);
		}

		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
