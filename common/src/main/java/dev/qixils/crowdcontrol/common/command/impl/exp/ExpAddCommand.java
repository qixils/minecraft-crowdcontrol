package dev.qixils.crowdcontrol.common.command.impl.exp;

import dev.qixils.crowdcontrol.common.Plugin;
import dev.qixils.crowdcontrol.common.command.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ExpAddCommand<P> implements ImmediateCommand<P> {
	private final @NotNull String effectName = "xp_add";
	private final @NotNull Plugin<P, ?> plugin;

	@Override
	public @NotNull Component getProcessedDisplayName(@NotNull Request request) {
		if (request.getParameters() == null)
			return getDefaultDisplayName();
		int amount = (int) (double) request.getParameters()[0];
		return getDefaultDisplayName().args(Component.text(amount));
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull P> players, @NotNull Request request) {
		if (request.getParameters() == null)
			return request.buildResponse().type(Response.ResultType.UNAVAILABLE).message("CC is improperly configured and failing to send parameters");
		int amount = (int) (double) request.getParameters()[0];
		sync(() -> players.stream().map(plugin::getPlayer).forEach(player -> player.addXpLevel(amount)));
		return request.buildResponse().type(Response.ResultType.SUCCESS).message("SUCCESS");
	}
}
