package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.qixils.crowdcontrol.common.CommandConstants.WEATHER_TICKS;

public abstract class AbstractWeatherCommand extends ImmediateCommand {
	protected static final int WEATHER_DURATION = (int) WEATHER_TICKS;

	public AbstractWeatherCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	protected abstract void applyWeather(World world);

	protected abstract boolean isWeatherActive(World world);

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		if (!isGlobalCommandUsable(players, request))
			return request.buildResponse().type(ResultType.UNAVAILABLE).message("Global command cannot be used on this streamer");

		Response.Builder result = request.buildResponse().type(Response.ResultType.FAILURE).message("This weather is already applied");
		for (World world : Bukkit.getWorlds()) {
			if (world.getEnvironment() != World.Environment.NORMAL)
				continue;
			if (isWeatherActive(world))
				continue;
			result.type(Response.ResultType.SUCCESS).message("SUCCESS");
			sync(() -> applyWeather(world));
		}
		return result;
	}
}
