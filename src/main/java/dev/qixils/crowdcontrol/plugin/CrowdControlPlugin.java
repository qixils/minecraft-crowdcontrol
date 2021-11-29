package dev.qixils.crowdcontrol.plugin;

import dev.qixils.crowdcontrol.CrowdControl;
import dev.qixils.crowdcontrol.plugin.utils.TextBuilder;
import dev.qixils.crowdcontrol.socket.Request;
import me.lucko.commodore.Commodore;
import me.lucko.commodore.CommodoreProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
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

public final class CrowdControlPlugin extends JavaPlugin implements Listener {
    public static final PersistentDataType<Byte, Boolean> BOOLEAN = new BooleanDataType();
    public static final TextColor USER_COLOR = TextColor.color(0x9f44db);
    public static final TextColor CMD_COLOR = TextColor.color(0xb15be3);
    private static final Component JOIN_MESSAGE_1 = new TextBuilder()
            .rawNext("This server is running ")
            .next("Crowd Control", TextColor.color(0xFAE100)) // picked a color from the CC logo/icon
            .rawNext(", developed by ")
            .next("qi", TextColor.color(0xFFC7B5))
            .next("xi", TextColor.color(0xFFDECA))
            .next("ls", TextColor.color(0xFFCEEA))
            .next(".dev", TextColor.color(0xFFB7E5))
            .rawNext(" in coordination with the ")
            .next("crowdcontrol.live", TextColor.color(0xFAE100))
            .rawNext(" team.")
            .build();
    private static final Component JOIN_MESSAGE_2 = new TextBuilder()
            .rawNext("Please link your Twitch account using ")
            .next("/account link <username>", NamedTextColor.GOLD)
            .rawNext(". You can ")
            .next("click here", TextDecoration.BOLD)
            .rawNext(" to do so.")
            .suggest("/account link ")
            .hover(Component.text("Click here to link your Twitch account").asHoverEvent())
            .build();
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

        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(mapper, this);
        Commodore commodore = CommodoreProvider.getCommodore(this);

        BukkitCrowdControlCommand.register(
                this,
                commodore,
                Objects.requireNonNull(getCommand("crowdcontrol"), "plugin.yml is improperly configured; cannot find crowdcontrol command")
        );

        BukkitAccountCommand.register(
                mapper,
                commodore,
                Objects.requireNonNull(getCommand("account"), "plugin.yml is improperly configured; cannot find account command")
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
    public List<@NotNull Player> getAllPlayers() {
        return mapper.getAllPlayers();
    }

    @CheckReturnValue
    @NotNull
    public List<@NotNull Player> getPlayers(final @NotNull Request request) {
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

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage(JOIN_MESSAGE_1);
        if (!global && !mapper.twitchToUserMap.containsValue(event.getPlayer().getUniqueId()))
            event.getPlayer().sendMessage(JOIN_MESSAGE_2);
    }

    // boilerplate stuff for the data container storage
    private static final class BooleanDataType implements PersistentDataType<Byte, Boolean> {
        private static final byte TRUE = 1;
        private static final byte FALSE = 0;

        @NotNull
        public Class<Byte> getPrimitiveType() {
            return Byte.class;
        }

        @NotNull
        public Class<Boolean> getComplexType() {
            return Boolean.class;
        }

        @NotNull
        public Byte toPrimitive(@NotNull Boolean complex, @NotNull PersistentDataAdapterContext context) {
            return complex ? TRUE : FALSE;
        }

        @NotNull
        public Boolean fromPrimitive(@NotNull Byte primitive, @NotNull PersistentDataAdapterContext context) {
            return primitive != FALSE;
        }
    }
}
