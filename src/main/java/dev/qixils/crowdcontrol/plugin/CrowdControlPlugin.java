package dev.qixils.crowdcontrol.plugin;

import dev.qixils.crowdcontrol.CrowdControl;
import dev.qixils.crowdcontrol.socket.Request;
import me.lucko.commodore.CommodoreProvider;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.CheckReturnValue;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class CrowdControlPlugin extends JavaPlugin {
    public static final TextColor USER_COLOR = TextColor.color(0x9f44db);
    public static final TextColor CMD_COLOR = TextColor.color(0xb15be3);
    private static final int port = 58431;
    final FileConfiguration config = getConfig();
    private final PlayerMapper mapper = new PlayerMapper(this);
    // actual stuff
    CrowdControl crowdControl = null;
    List<Command> commands;
    private boolean isServer = true;
    private boolean global = false;
    private Collection<String> hosts = Collections.emptyList();
    private boolean announce = true;

    @Override
    public void onLoad() {
        saveDefaultConfig();
    }

    void initCrowdControl() {
        String password = config.getString("password", "");
        String ip = config.getString("ip", "127.0.0.1");

        if (!password.isBlank()) {
            getLogger().info("Running Crowd Control in server mode");
            isServer = true;
            crowdControl = CrowdControl.server().port(port).password(password).build();
        } else if (!ip.isBlank()) {
            getLogger().info("Running Crowd Control in client mode");
            isServer = false;
            crowdControl = CrowdControl.client().port(port).ip(ip).build();
        } else {
            throw new IllegalStateException("Config file is improperly configured; please ensure you have entered a valid IP address or password.");
        }

        if (commands == null)
            commands = RegisterCommands.register(this);
        else
            RegisterCommands.register(this, commands);
    }

    @Override
    public void onEnable() {
        global = config.getBoolean("global", false);
        announce = config.getBoolean("announce", true);
        hosts = config.getStringList("hosts");
        if (!hosts.isEmpty()) {
            Set<String> loweredHosts = new HashSet<>(hosts.size());
            for (String host : hosts)
                loweredHosts.add(host.toLowerCase(Locale.ENGLISH));
            hosts = loweredHosts;
        }

        initCrowdControl();

        Bukkit.getPluginManager().registerEvents(mapper, this);

        BukkitCrowdControlCommand.register(
                this,
                CommodoreProvider.getCommodore(this),
                Objects.requireNonNull(getCommand("crowdcontrol"), "plugin.yml is improperly configured; cannot find crowdcontrol command")
        );
    }

    @Override
    public void onDisable() {
        if (crowdControl == null) return;
        crowdControl.shutdown("Plugin is unloading (server may be shutting down)");
        crowdControl = null;
        commands = null;
    }

    public Collection<String> getHosts() {
        return hosts;
    }

    public boolean announceEffects() {
        return announce;
    }

    @CheckReturnValue
    public boolean isGlobal(@NotNull Request request) {
        return global || request.isGlobal();
    }

    @CheckReturnValue
    @NotNull
    public CompletableFuture<@NotNull List<@NotNull Player>> getPlayers(final @NotNull Request request) {
        return mapper.getPlayers(request);
    }

    public void registerCommand(@NotNull String name, @NotNull Command command) {
        name = name.toLowerCase(Locale.ENGLISH);
        crowdControl.registerHandler(name, command::executeAndNotify);
        getLogger().fine("Registered CC command '" + name + "'");
    }

    @Nullable
    public CrowdControl getCrowdControl() {
        return crowdControl;
    }

    public boolean isServer() {
        return isServer;
    }
}
