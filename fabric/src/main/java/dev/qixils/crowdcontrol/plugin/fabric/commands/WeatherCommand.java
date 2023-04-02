package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.Global;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.WEATHER_TICKS;

@Getter
@Global
public class WeatherCommand extends ImmediateCommand {
	private final int ticks = (int) WEATHER_TICKS;
	private final String effectName;
	private final boolean rain;
	private final boolean storm;

	private WeatherCommand(FabricCrowdControlPlugin plugin, String effectName, boolean rain, boolean storm) {
		super(plugin);
		this.effectName = effectName;
		this.rain = rain;
		this.storm = storm;
		if (storm && !rain)
			throw new IllegalArgumentException("Storms can only be used with rain");
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayerEntity> players, @NotNull Request request) {
		Response.Builder builder = request.buildResponse().type(ResultType.FAILURE).message("Requested weather is already active");
		for (ServerWorld world : plugin.server().getWorlds()) {
			if (!world.getRegistryKey().getValue().getPath().equals("overworld"))
				continue;
			if (storm && world.isThundering())
				continue;
			if (rain && world.isRaining())
				continue;
			if (!rain && !world.isRaining() && !world.isThundering())
				continue;
			if (!rain)
				sync(() -> world.setWeather(ticks, 0, false, false));
			else
				sync(() -> world.setWeather(0, ticks, true, storm));
			builder.type(ResultType.SUCCESS).message("SUCCESS");
		}
		return builder;
	}

	public static WeatherCommand clear(FabricCrowdControlPlugin plugin) {
		return new WeatherCommand(plugin, "clear", false, false);
	}

	public static WeatherCommand downfall(FabricCrowdControlPlugin plugin) {
		return new WeatherCommand(plugin, "downfall", true, false);
	}

	public static WeatherCommand storm(FabricCrowdControlPlugin plugin) {
		return new WeatherCommand(plugin, "thunder_storm", true, true);
	}
}
