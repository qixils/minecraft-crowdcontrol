package dev.qixils.crowdcontrol.common.command.impl.exp;

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
public class ExpSubCommand<P> implements ImmediateCommand<P> {
	private final @NotNull String effectName = "xp_sub";
	private final @NotNull QuantityStyle quantityStyle = QuantityStyle.APPEND;
	private final @NotNull Plugin<P, ?> plugin;

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull P> players, @NotNull Request request) {
		Response.Builder resp = request.buildResponse().type(Response.ResultType.RETRY).message("Player does not have enough XP levels");
		int amount = request.getQuantityOrDefault();

		for (P rawPlayer : players) {
			CCPlayer player = plugin.getPlayer(rawPlayer);
			int curLevel = player.xpLevel();
			int newLevel = curLevel - amount;
			if (newLevel >= 0) {
				sync(rawPlayer, () -> player.xpLevel(newLevel));
				resp.type(Response.ResultType.SUCCESS).message("SUCCESS");
			}
		}
		return resp;
	}
}
