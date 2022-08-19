package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.Global;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.qixils.crowdcontrol.common.CommandConstants.WEATHER_TICKS;

@Getter
@Global
public class WeatherCommand extends ImmediateCommand {
	private final int ticks = (int) WEATHER_TICKS;
	private final String effectName;
	private final String displayName;
	private final boolean rain;
	private final boolean storm;

	public WeatherCommand(FabricCrowdControlPlugin plugin, String effectName, String displayName, boolean rain, boolean storm) {
		super(plugin);
		this.effectName = effectName;
		this.displayName = displayName;
		this.rain = rain;
		this.storm = storm;
		if (storm && !rain)
			throw new IllegalArgumentException("Storms can only be used with rain");
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder builder = request.buildResponse().type(ResultType.FAILURE).message("Requested weather is already active");
		for (ServerLevel world : plugin.server().getAllLevels()) {
			if (!world.dimension().location().getPath().equals("overworld"))
				continue;
			if (storm && world.isThundering())
				continue;
			if (rain && world.isRaining())
				continue;
			if (!rain && !world.isRaining() && !world.isThundering())
				continue;
			if (!rain)
				sync(() -> world.setWeatherParameters(ticks, 0, false, false));
			else
				sync(() -> world.setWeatherParameters(0, ticks, true, storm));
			builder.type(ResultType.SUCCESS).message("SUCCESS");
		}
		return builder;
	}

	public static WeatherCommand clear(FabricCrowdControlPlugin plugin) {
		return new WeatherCommand(plugin, "clear", "Set Weather to Clear", false, false);
	}

	public static WeatherCommand downfall(FabricCrowdControlPlugin plugin) {
		return new WeatherCommand(plugin, "downfall", "Set Weather to Rain", true, false);
	}

	public static WeatherCommand storm(FabricCrowdControlPlugin plugin) {
		return new WeatherCommand(plugin, "thunder_storm", "Set Weather to Thunder Storm", true, true);
	}
}
