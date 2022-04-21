package dev.qixils.crowdcontrol.plugin.mojmap.commands;

import dev.qixils.crowdcontrol.plugin.mojmap.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.mojmap.MojmapPlugin;
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
	private final String displayName;
	private final int amount;

	public FeedCommand(MojmapPlugin plugin, String effectName, String displayName, int amount) {
		super(plugin);
		this.effectName = effectName;
		this.displayName = displayName;
		this.amount = amount;
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder resp = request.buildResponse().type(ResultType.FAILURE).message("Player's hunger is already max or empty");
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
