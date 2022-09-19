package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.manipulator.mutable.entity.FoodData;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.entity.living.player.Player;

import java.util.List;

@Getter
public class FeedCommand extends ImmediateCommand {
	private final String effectName;
	private final int amount;

	public FeedCommand(SpongeCrowdControlPlugin plugin, String effectName, int amount) {
		super(plugin);
		this.effectName = effectName;
		this.amount = amount;
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder resp = request.buildResponse().type(ResultType.RETRY).message("Player's hunger is already max or empty");
		for (Player player : players) {
			FoodData data = player.getFoodData();
			MutableBoundedValue<Integer> foodData = data.foodLevel();
			int currFood = foodData.get();
			MutableBoundedValue<Double> saturationData = player.saturation();
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
