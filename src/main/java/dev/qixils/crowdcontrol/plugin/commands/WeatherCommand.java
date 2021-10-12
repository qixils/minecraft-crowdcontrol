package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.utils.RandomUtil;
import dev.qixils.crowdcontrol.plugin.utils.TextUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

@Getter
public class WeatherCommand extends ImmediateCommand {
    protected final String effectName;
    protected final String displayName;
    protected final WeatherType weatherType;
    protected static final int DURATION = 20*60*60;
    public WeatherCommand(CrowdControlPlugin plugin, WeatherType weatherType) {
        super(plugin);
        this.weatherType = weatherType;
        this.effectName = weatherType.name();
        this.displayName = "Set Weather to " + TextUtil.titleCase(weatherType);
    }

    @Override
    public Response.@NotNull Builder executeImmediately(@NotNull Request request) {
        Response.Builder result = Response.builder().type(Response.ResultType.FAILURE).message("This weather is already applied");
        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() != World.Environment.NORMAL) continue;

            if (weatherType == WeatherType.DOWNFALL) {
                if (world.getClearWeatherDuration() > 0) {
                    result.type(Response.ResultType.SUCCESS).message("SUCCESS");
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        world.setStorm(true);
                        if (RandomUtil.RNG.nextBoolean())
                            world.setThundering(true);
                    });
                }
            } else if (world.getClearWeatherDuration() <= 0) {
                result.type(Response.ResultType.SUCCESS).message("SUCCESS");
                Bukkit.getScheduler().runTask(plugin, () -> {
                    world.setWeatherDuration(0);
                    world.setStorm(false);
                    world.setClearWeatherDuration(DURATION);
                });
            }
        }
        return result;
    }
}
