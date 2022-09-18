package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import lombok.Getter;
import org.bukkit.World;

@Getter
public class ThunderingWeatherCommand extends AbstractWeatherCommand {
	private final String effectName = "thunder_storm";

	public ThunderingWeatherCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	protected boolean isWeatherActive(World world) {
		return world.isThundering();
	}

	@Override
	protected void applyWeather(World world) {
		world.setClearWeatherDuration(0);
		world.setStorm(true);
		world.setThundering(true);
		world.setWeatherDuration(WEATHER_DURATION);
		world.setThunderDuration(WEATHER_DURATION);
	}
}
