package dev.qixils.crowdcontrol.common.command.impl.exp;

import dev.qixils.crowdcontrol.common.Plugin;
import dev.qixils.crowdcontrol.common.command.ImmediateCommand;
import dev.qixils.crowdcontrol.common.command.QuantityStyle;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ExpAddCommand<P> implements ImmediateCommand<P> {
	private final @NotNull String effectName = "xp_add";
	private final @NotNull QuantityStyle quantityStyle = QuantityStyle.APPEND;
	private final @NotNull Plugin<P, ?> plugin;

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull P> players, @NotNull Request request) {
		int amount = request.getQuantityOrDefault();
		sync(() -> players.stream().map(plugin::getPlayer).forEach(player -> player.addXpLevel(amount)));
		return request.buildResponse().type(Response.ResultType.SUCCESS).message("SUCCESS");
	}
}
