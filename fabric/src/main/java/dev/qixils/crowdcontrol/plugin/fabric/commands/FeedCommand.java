package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class FeedCommand extends ImmediateCommand {
	private final String effectName;
	private final int amount;

	public FeedCommand(FabricCrowdControlPlugin plugin, String effectName, int amount) {
		super(plugin);
		this.effectName = effectName;
		this.amount = amount;
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder resp = request.buildResponse().type(ResultType.RETRY).message("Player's hunger is already max or empty");
		for (ServerPlayer player : players) {
			FoodData foodData = player.getFoodData();
			int currFood = foodData.getFoodLevel();
			float currSaturation = foodData.getSaturationLevel();

			int newFood = Math.max(0, Math.min(20, currFood + amount));

			if (newFood != currFood) {
				sync(() -> foodData.setFoodLevel(newFood));
				resp.type(ResultType.SUCCESS).message("SUCCESS");
			}

			float newSaturation = Math.max(0, Math.min(newFood, currSaturation + currFood + amount - 20));
			if ((currFood + amount) > 20 && Math.abs(newSaturation - currSaturation) < .01) {
				sync(() -> foodData.setSaturation(newSaturation));
				resp.type(ResultType.SUCCESS).message("SUCCESS");
			} else if (newFood == 0 && currSaturation > 0.01) {
				sync(() -> foodData.setSaturation(0));
				resp.type(ResultType.SUCCESS).message("SUCCESS");
			}
		}
		return resp;
	}
}
