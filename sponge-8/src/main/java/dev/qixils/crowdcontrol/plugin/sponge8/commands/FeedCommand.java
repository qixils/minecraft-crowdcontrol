package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.List;

@Getter
public class FeedCommand extends ImmediateCommand {
	private final String effectName;
	private final String displayName;
	private final int amount;

	public FeedCommand(SpongeCrowdControlPlugin plugin, String effectName, String displayName, int amount) {
		super(plugin);
		this.effectName = effectName;
		this.displayName = displayName;
		this.amount = amount;
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder resp = request.buildResponse().type(ResultType.FAILURE).message("Player's hunger is already max or empty");
		for (ServerPlayer player : players) {
			Value.Mutable<Integer> foodData = player.foodLevel();
			int currFood = foodData.get();
			Value.Mutable<Double> saturationData = player.saturation();
			double currSaturation = saturationData.get();

			int newFood = Math.max(0, Math.min(20, currFood + amount));

			if (newFood != currFood) {
				sync(() -> player.offer(foodData.set(newFood)));
				resp.type(ResultType.SUCCESS).message("SUCCESS");
			}

			double newSaturation = Math.max(0, Math.min(newFood, currSaturation + currFood + amount - 20));
			if ((currFood + amount) > 20 && Math.abs(newSaturation - currSaturation) < .01) {
				sync(() -> player.offer(saturationData.set(newSaturation)));
				resp.type(ResultType.SUCCESS).message("SUCCESS");
			} else if (newFood == 0 && currSaturation > 0.01) {
				sync(() -> player.offer(saturationData.set(0d)));
				resp.type(ResultType.SUCCESS).message("SUCCESS");
			}
		}
		return resp;
	}
}
