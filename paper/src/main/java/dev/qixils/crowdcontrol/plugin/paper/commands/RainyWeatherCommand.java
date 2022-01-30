package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import lombok.Getter;
import org.bukkit.World;

@Getter
public class RainyWeatherCommand extends AbstractWeatherCommand {
	private final String effectName = "downfall";
	private final String displayName = "Set Weather to Downfall";

	public RainyWeatherCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	protected boolean isWeatherActive(World world) {
		return !world.isClearWeather() && !world.isThundering();
	}

	@Override
	protected void applyWeather(World world) {
		world.setClearWeatherDuration(0);
		world.setStorm(true);
		world.setThundering(false);
		world.setWeatherDuration(WEATHER_DURATION);
	}
}
