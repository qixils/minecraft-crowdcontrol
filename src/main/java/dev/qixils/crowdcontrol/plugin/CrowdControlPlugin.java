package dev.qixils.crowdcontrol.plugin;

import dev.qixils.crowdcontrol.CrowdControl;
import me.lucko.commodore.CommodoreProvider;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public final class CrowdControlPlugin extends JavaPlugin {
    // actual stuff
    CrowdControl crowdControl = null;
    List<Command> commands;
    final FileConfiguration config = getConfig();
    public static final TextColor USER_COLOR = TextColor.color(0x9f44db);
    public static final TextColor CMD_COLOR = TextColor.color(0xb15be3);

    @Override
    public void onLoad() {
        config.addDefault("ip", "127.0.0.1");
        config.addDefault("port", 58431);
        config.options().copyDefaults(true);
        saveConfig();
    }

    void initCrowdControl() {
        String ip = config.getString("ip");
        int port = config.getInt("port");
        if (ip == null || port == 0) {
            throw new IllegalStateException("Config file is misconfigured, please ensure you have entered a valid IP address and port.");
        }
        crowdControl = new CrowdControl(ip, port);
        crowdControl.registerCheck(() -> !getPlayers().isEmpty());
        if (commands == null)
            commands = RegisterCommands.register(this);
        else
            RegisterCommands.register(this, commands);
    }

    @Override
    public void onEnable() {
        initCrowdControl();

        BukkitCrowdControlCommand.register(
                this,
                CommodoreProvider.getCommodore(this),
                Objects.requireNonNull(getCommand("crowdcontrol"), "plugin.yml is misconfigured; cannot find crowdcontrol command")
        );
    }

    @Override
    public void onDisable() {
        if (crowdControl == null) return;
        crowdControl.shutdown();
        crowdControl = null;
        commands = null;
    }

    public static List<Player> getPlayers() {
        return Bukkit.getServer().getOnlinePlayers().stream()
                .filter(player -> !player.isDead() && player.getGameMode() != GameMode.SPECTATOR)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void registerCommand(@NotNull String name, @NotNull Command command) {
        name = name.toLowerCase(Locale.ENGLISH);
        crowdControl.registerHandler(name, command::executeAndNotify);
        getLogger().fine("Registered CC command '"+name+"'");
    }

    @Nullable
    public CrowdControl getCrowdControl() {
        return crowdControl;
    }
}
