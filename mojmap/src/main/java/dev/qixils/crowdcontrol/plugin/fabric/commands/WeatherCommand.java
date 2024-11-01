package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.Global;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.WEATHER_TICKS;

@Getter
@Global
public class WeatherCommand extends ModdedCommand {
	private final int ticks = (int) WEATHER_TICKS;
	private final String effectName;
	private final boolean rain;
	private final boolean storm;

	private WeatherCommand(ModdedCrowdControlPlugin plugin, String effectName, boolean rain, boolean storm) {
		super(plugin);
		this.effectName = effectName;
		this.rain = rain;
		this.storm = storm;
		if (storm && !rain)
			throw new IllegalArgumentException("Storms can only be used with rain");
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(() -> {
			boolean success = false;
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
				success = true;
			}
			return success
				? new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS)
				: new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Requested weather is already active");
		}));
	}

	public static WeatherCommand clear(ModdedCrowdControlPlugin plugin) {
		return new WeatherCommand(plugin, "clear", false, false);
	}

	public static WeatherCommand downfall(ModdedCrowdControlPlugin plugin) {
		return new WeatherCommand(plugin, "downfall", true, false);
	}

	public static WeatherCommand storm(ModdedCrowdControlPlugin plugin) {
		return new WeatherCommand(plugin, "thunder_storm", true, true);
	}
}
