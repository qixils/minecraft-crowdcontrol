package dev.qixils.crowdcontrol.common.command.impl.food;

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
public class FoodAddCommand<P> implements ImmediateCommand<P> {
	private final @NotNull String effectName = "feed";
	private final @NotNull QuantityStyle quantityStyle = QuantityStyle.APPEND;
	private final @NotNull Plugin<P, ?> plugin;

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull P> players, @NotNull Request request) {
		int amount = request.getQuantityOrDefault() * 2;

		Response.Builder resp = request.buildResponse().type(Response.ResultType.RETRY).message("Player's hunger is already max");
		for (P rawPlayer : players) {
			CCPlayer player = plugin.getPlayer(rawPlayer);
			int currFood = player.foodLevel();
			double currSaturation = player.saturation();

			int newFood = Math.min(20, currFood + amount);
			// don't apply effect unless it is 100% utilized
			if ((newFood - currFood) == amount || (newFood - currSaturation) >= amount) {
				sync(() -> {
					player.foodLevel(newFood);
					player.saturation(newFood);
				});
				resp.type(Response.ResultType.SUCCESS).message("SUCCESS");
			}
		}
		return resp;
	}
}
