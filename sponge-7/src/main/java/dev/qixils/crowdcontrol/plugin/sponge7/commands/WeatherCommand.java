package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge7.utils.Sponge7TextUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.weather.Weather;
import org.spongepowered.api.world.weather.Weathers;

import java.util.List;

import static dev.qixils.crowdcontrol.common.CommandConstants.WEATHER_TICKS;

@Getter
public class WeatherCommand extends ImmediateCommand {
	private final String effectName;
	private final String displayName;
	private final Weather weather;

	public WeatherCommand(SpongeCrowdControlPlugin plugin, Weather weather) {
		super(plugin);
		this.weather = weather;
		this.displayName = "Set Weather to " + Sponge7TextUtil.titleCase(Sponge7TextUtil.valueOf(weather));
		this.effectName = weather.equals(Weathers.RAIN)
				? "downfall"
				: Sponge7TextUtil.valueOf(weather);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		if (!isGlobalCommandUsable(players, request))
			return request.buildResponse().type(ResultType.UNAVAILABLE).message("Global command cannot be used on this streamer");

		Response.Builder builder = request.buildResponse().type(ResultType.FAILURE).message("Requested weather is already active");
		for (World world : plugin.getGame().getServer().getWorlds()) {
			if (!world.getDimension().getType().equals(DimensionTypes.OVERWORLD))
				continue;
			if (world.getWeather().equals(weather))
				continue;
			sync(() -> world.setWeather(weather, WEATHER_TICKS));
			builder.type(ResultType.SUCCESS).message("SUCCESS");
		}
		return builder;
	}
}
