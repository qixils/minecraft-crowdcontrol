package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.common.Global;
import dev.qixils.crowdcontrol.common.util.TextUtil;
import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.WorldTypes;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.weather.WeatherType;
import org.spongepowered.api.world.weather.WeatherTypes;

import java.util.List;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.WEATHER_TICKS;

@Getter
@Global
public class WeatherCommand extends ImmediateCommand {
	private final Ticks weatherTicks = Ticks.of(WEATHER_TICKS);
	private final String effectName;
	private final String displayName;
	private final WeatherType weather;

	public WeatherCommand(SpongeCrowdControlPlugin plugin, WeatherType weather) {
		super(plugin);
		this.weather = weather;

		String valueKey = weather.key(RegistryTypes.WEATHER_TYPE).value();
		this.displayName = "Set Weather to " + TextUtil.titleCase(valueKey);

		if (weather.equals(WeatherTypes.RAIN.get())) {
			this.effectName = "downfall";
		} else if (weather.equals(WeatherTypes.THUNDER.get())) {
			this.effectName = "thunder_storm";
		} else {
			this.effectName = valueKey;
		}
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder builder = request.buildResponse().type(ResultType.FAILURE).message("Requested weather is already active");
		for (ServerWorld world : plugin.getGame().server().worldManager().worlds()) {
			if (!world.worldType().equals(WorldTypes.OVERWORLD.get()))
				continue;
			if (world.weather().type().equals(weather))
				continue;
			sync(() -> world.setWeather(weather, weatherTicks));
			builder.type(ResultType.SUCCESS).message("SUCCESS");
		}
		return builder;
	}
}
