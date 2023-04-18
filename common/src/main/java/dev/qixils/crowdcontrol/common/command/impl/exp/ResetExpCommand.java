package dev.qixils.crowdcontrol.common.command.impl.exp;

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
public class ResetExpCommand<P> implements ImmediateCommand<P> {
	private final @NotNull String effectName = "reset_exp_progress";
	private final @NotNull Plugin<P, ?> plugin;

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull P> players, @NotNull Request request) {
		Response.Builder resp = request.buildResponse().type(Response.ResultType.RETRY).message("Player does not have any XP levels");
		for (P rawPlayer : players) {
			CCPlayer player = plugin.getPlayer(rawPlayer);
			if (player.xpLevel() > 0) {
				sync(rawPlayer, () -> player.xpLevel(0));
				resp.type(Response.ResultType.SUCCESS).message("SUCCESS");
			}
		}
		return resp;
	}
}
