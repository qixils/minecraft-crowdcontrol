package io.github.lexikiq.crowdcontrol.commands;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import io.github.lexikiq.crowdcontrol.ChatCommand;
import io.github.lexikiq.crowdcontrol.ClassCooldowns;
import io.github.lexikiq.crowdcontrol.CrowdControl;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WeatherCommand extends ChatCommand {
    protected final WeatherType weatherType;
    protected static final int DURATION = 20*60*60;
    public WeatherCommand(CrowdControl plugin, WeatherType weatherType) {
        super(plugin);
        this.weatherType = weatherType;
    }

    @Override
    public int getCooldownSeconds() {
        return 0;
    }

    @Override
    public ClassCooldowns getClassCooldown() {
        return ClassCooldowns.WEATHER;
    }

    @Override
    public @NotNull String getCommand() {
        return weatherType.name();
    }

    @Override
    public boolean execute(ChannelMessageEvent event, List<Player> players, String... args) {
        for (Player player : players) {
            World world = player.getWorld();
            if (weatherType == WeatherType.CLEAR) {
                world.setWeatherDuration(0);
                world.setClearWeatherDuration(DURATION);
            } else {
                world.setClearWeatherDuration(0);
                // needs to be BukkitRunnable and seems to not matter
                // player.getServer().dispatchCommand(player.getServer().getConsoleSender(), "weather rain");
            }
        }
        return true;
    }
}
