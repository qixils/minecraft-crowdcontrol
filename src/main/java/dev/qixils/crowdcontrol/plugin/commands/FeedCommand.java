package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.Command;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

@Getter
public class FeedCommand extends Command {
	private final String effectName;
	private final String displayName;
	private final int amount;

	public FeedCommand(CrowdControlPlugin plugin, String effectName, String displayName, int amount) {
		super(plugin);
		this.effectName = effectName;
		this.displayName = displayName;
		this.amount = amount;
	}

	@Override
	public Response.@NotNull Result execute(@NotNull Request request) {
		Bukkit.getScheduler().runTask(plugin, () -> CrowdControlPlugin.getPlayers().forEach(player -> {
			int currFood = player.getFoodLevel();
			int newFood = Math.max(0, Math.min(20, currFood + amount));
			player.setFoodLevel(newFood);
			if (amount > 0 && currFood + amount > 20)
				player.setSaturation(player.getSaturation() + currFood + amount - 20);
			else if (newFood == 0)
				player.setSaturation(0);
		}));
		return Response.Result.SUCCESS;
	}
}
