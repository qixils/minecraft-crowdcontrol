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
import org.bukkit.command.PluginCommand;
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
    static final String PREFIX = "CrowdControl";
    public static final PersistentDataType<Byte, Boolean> BOOLEAN = new BooleanDataType();
    public static final TextColor USER_COLOR = TextColor.color(0x9f44db);
    public static final TextColor CMD_COLOR = TextColor.color(0xb15be3);
    private static final Component JOIN_MESSAGE_1 = new TextBuilder(TextColor.color(0xFCE9D4))
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
    private static final Component JOIN_MESSAGE_2 = new TextBuilder(TextColor.color(0xF1D4FC))
            .rawNext("Please link your Twitch account using ")
            .next("/account link <username>", NamedTextColor.GOLD)
            .rawNext(". You can ")
            .next("click here", TextDecoration.BOLD)
            .rawNext(" to do so.")
            .suggest("/account link ")
            .hover(Component.text("Click here to link your Twitch account").asHoverEvent())
            .build();
    private static final TextColor _ERROR_COLOR = TextColor.color(0xF78080);
    private static final Component NO_CC_USER_ERROR = new TextBuilder(_ERROR_COLOR)
            .next("WARNING: ", NamedTextColor.RED)
            .rawNext("The Crowd Control plugin has failed to load. Please ask a server administrator to the console logs and address the error.")
            .build();
    private static final Component NO_CC_OP_ERROR_NO_PASSWORD = new TextBuilder(_ERROR_COLOR)
            .next("WARNING: ", NamedTextColor.RED)
            .rawNext("The Crowd Control plugin has failed to load due to a password not being set. Please use ")
            .next("/password <password>", NamedTextColor.GOLD)
            .rawNext(" to set a password and ")
            .next("/crowdcontrol reconnect", NamedTextColor.GOLD)
            .next(" to properly load the plugin. And be careful not to show the password on stream!")
            .suggest("/password ")
            .hover(Component.text("Click here to set the password").asHoverEvent())
            .build();
    private static final Component NO_CC_UNKNOWN_ERROR = new TextBuilder(_ERROR_COLOR)
            .next("WARNING: ", NamedTextColor.RED)
            .rawNext("The Crowd Control plugin has failed to load. Please review the console logs and resolve the error.")
            .build();
    private static final int port = 58431;
    FileConfiguration config = getConfig();
    private final PlayerMapper mapper = new PlayerMapper(this);
    // actual stuff
    String manualPassword = null; // set via /password
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
        reloadConfig();
        config = getConfig();
        String password = Objects.requireNonNullElseGet(manualPassword, () -> config.getString("password", ""));
        String ip = config.getString("ip", "127.0.0.1");

        if (!config.getBoolean("legacy", false)) {
            isServer = true;
            if (!password.isBlank()) {
                getLogger().info("Running Crowd Control in server mode");
                crowdControl = CrowdControl.server().port(port).password(password).build();
            } else {
                getLogger().severe("No password has been set in the plugin's config file. Please set one by editing plugins/CrowdControl/config.yml or enable a temporary password using the /password command.");
                return;
            }
        } else {
            isServer = false;
            if (ip.isBlank())
                throw new IllegalStateException("IP address is blank. Please fix this in the config.yml file");
            getLogger().info("Running Crowd Control in client mode");
            crowdControl = CrowdControl.client().port(port).ip(ip).build();
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
                getCommand("crowdcontrol")
        );

        BukkitAccountCommand.register(
                mapper,
                commodore,
                getCommand("account")
        );

        BukkitPasswordCommand.register(
                this,
                commodore,
                getCommand("password")
        );
    }

    @Override
    public @NotNull PluginCommand getCommand(@NotNull String name) {
        return Objects.requireNonNull(super.getCommand(name), "plugin.yml is improperly configured; cannot find " + name + " command");
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
        if (crowdControl == null) {
            if (event.getPlayer().isOp()) {
                if (isServer && manualPassword == null)
                    event.getPlayer().sendMessage(NO_CC_OP_ERROR_NO_PASSWORD);
                else
                    event.getPlayer().sendMessage(NO_CC_UNKNOWN_ERROR);
            } else
                event.getPlayer().sendMessage(NO_CC_USER_ERROR);
        }
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
