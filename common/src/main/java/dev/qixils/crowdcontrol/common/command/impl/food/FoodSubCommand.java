package dev.qixils.crowdcontrol.common.command.impl.food;

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
public class FoodSubCommand<P> implements ImmediateCommand<P> {
	private final @NotNull String effectName = "starve";
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
		int amount = (int) request.getParameters()[0] * 2;

		Response.Builder resp = request.buildResponse().type(Response.ResultType.RETRY).message("Player's hunger is already empty");
		for (P rawPlayer : players) {
			CCPlayer player = plugin.getPlayer(rawPlayer);
			int currFood = player.foodLevel();
			double currSaturation = player.saturation();

			int newFood = Math.max(0, currFood - amount);
			double newSaturation = Math.min(newFood, currSaturation);
			// don't apply effect unless it is 100% utilized
			if ((currFood - newFood) == amount) {
				sync(() -> {
					player.foodLevel(newFood);
					player.saturation(newSaturation);
				});
				resp.type(Response.ResultType.SUCCESS).message("SUCCESS");
			}
		}
		return resp;
	}
}
