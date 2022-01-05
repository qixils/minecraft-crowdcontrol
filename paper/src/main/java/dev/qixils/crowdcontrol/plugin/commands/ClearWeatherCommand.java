package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.BukkitCrowdControlPlugin;
import lombok.Getter;
import org.bukkit.World;

@Getter
public class ClearWeatherCommand extends AbstractWeatherCommand {
	private final String effectName = "clear";
	private final String displayName = "Set Weather to Clear";

	public ClearWeatherCommand(BukkitCrowdControlPlugin plugin) {
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
