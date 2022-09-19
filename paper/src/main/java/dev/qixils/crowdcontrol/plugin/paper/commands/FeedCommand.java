package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class FeedCommand extends ImmediateCommand {
	private final String effectName;
	private final int amount;

	public FeedCommand(PaperCrowdControlPlugin plugin, String effectName, int amount) {
		super(plugin);
		this.effectName = effectName;
		this.amount = amount;
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder resp = request.buildResponse().type(ResultType.RETRY).message("Player's hunger is already max or empty");
		for (Player player : players) {
			int currFood = player.getFoodLevel();
			float currSaturation = player.getSaturation();

			int newFood = Math.max(0, Math.min(20, currFood + amount));

			if (newFood != currFood) {
				sync(() -> player.setFoodLevel(newFood));
				resp.type(ResultType.SUCCESS).message("SUCCESS");
			}

			float newSaturation = Math.max(0, Math.min(newFood, currSaturation + currFood + amount - 20));
			if ((currFood + amount) > 20 && Math.abs(newSaturation - currSaturation) < .01) {
				sync(() -> player.setSaturation(newSaturation));
				resp.type(ResultType.SUCCESS).message("SUCCESS");
			} else if (newFood == 0 && currSaturation > 0.01) {
				sync(() -> player.setSaturation(0));
				resp.type(ResultType.SUCCESS).message("SUCCESS");
			}
		}
		return resp;
	}
}
