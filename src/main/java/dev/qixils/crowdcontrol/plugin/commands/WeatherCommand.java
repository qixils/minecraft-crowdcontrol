package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.ChatCommand;
import dev.qixils.crowdcontrol.plugin.ClassCooldowns;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WeatherCommand extends ChatCommand {
    protected final WeatherType weatherType;
    protected static final int DURATION = 20*60*60;
    public WeatherCommand(CrowdControlPlugin plugin, WeatherType weatherType) {
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
    public boolean execute(String authorName, List<Player> players, String... args) {
        for (Player player : players) {
            World world = player.getWorld();
            if (weatherType == WeatherType.CLEAR) {
                world.setClearWeatherDuration(DURATION);
            } else {
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        world.setStorm(true);
                    }
                }.runTask(plugin);
                // needs to be BukkitRunnable and seems to not matter
                // player.getServer().dispatchCommand(player.getServer().getConsoleSender(), "weather rain");
            }
        }
        return true;
    }
}
