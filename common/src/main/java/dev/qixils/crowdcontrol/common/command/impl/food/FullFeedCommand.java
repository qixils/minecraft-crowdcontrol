package dev.qixils.crowdcontrol.common.command.impl.food;

import dev.qixils.crowdcontrol.common.Plugin;
import dev.qixils.crowdcontrol.common.command.ImmediateCommand;
import dev.qixils.crowdcontrol.common.mc.CCPlayer;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class FullFeedCommand<P> implements ImmediateCommand<P> {
	private final @NotNull String effectName = "full_feed";
	private final @NotNull Plugin<P, ?> plugin;

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull P> players, @NotNull Request request) {
		Response.Builder resp = request.buildResponse().type(Response.ResultType.RETRY).message("Player's hunger is already max");
		for (P rawPlayer : players) {
			CCPlayer player = plugin.getPlayer(rawPlayer);

			if (player.foodLevel() < 20) {
				sync(() -> player.foodLevel(20));
				resp.type(Response.ResultType.SUCCESS).message("SUCCESS");
			}

			if (player.saturation() < 20) {
				sync(() -> player.saturation(20));
				resp.type(Response.ResultType.SUCCESS).message("SUCCESS");
			}
		}
		return resp;
	}
}
