package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import lombok.Getter;
import org.bukkit.World;

@Getter
public class ClearWeatherCommand extends AbstractWeatherCommand {
	private final String effectName = "clear";

	public ClearWeatherCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	protected boolean isWeatherActive(World world) {
		return world.isClearWeather();
	}

	@Override
	protected void applyWeather(World world) {
		world.setWeatherDuration(0);
		world.setStorm(false);
		world.setClearWeatherDuration(WEATHER_DURATION);
	}
}
