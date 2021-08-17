package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.ChatCommand;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.utils.TextUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.WeatherType;
import org.bukkit.World;

@Getter
public class WeatherCommand extends ChatCommand {
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
    public Response.Result execute(Request request) {
        for (World world : Bukkit.getWorlds()) {
            if (weatherType == WeatherType.CLEAR) {
                world.setClearWeatherDuration(DURATION);
            } else {
                world.setStorm(true);
                // needs to be BukkitRunnable and seems to not matter
                // player.getServer().dispatchCommand(player.getServer().getConsoleSender(), "weather rain");
            }
        }
        return Response.Result.SUCCESS;
    }
}
