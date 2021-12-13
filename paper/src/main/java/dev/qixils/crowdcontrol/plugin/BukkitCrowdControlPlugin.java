package dev.qixils.crowdcontrol.plugin;

import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import dev.qixils.crowdcontrol.CrowdControl;
import dev.qixils.crowdcontrol.common.Plugin;
import dev.qixils.crowdcontrol.common.util.TextUtil;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
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
import java.util.function.Function;

public final class BukkitCrowdControlPlugin extends JavaPlugin implements Listener, Plugin<Player, CommandSender> {
    public static final PersistentDataType<Byte, Boolean> BOOLEAN = new BooleanDataType();
    FileConfiguration config = getConfig();
    @Getter
    private final BukkitPlayerMapper playerMapper = new BukkitPlayerMapper(this);
    @Getter
    private final TextUtil textUtil = new TextUtil(Bukkit.getUnsafe().componentFlattener());
    @Getter
    private PaperCommandManager<CommandSender> commandManager;
    @Getter
    private final Class<Player> playerClass = Player.class;
    @Getter
    private final Class<CommandSender> commandSenderClass = CommandSender.class;
    // actual stuff
    String manualPassword = null; // set via /password
    @Getter
    CrowdControl crowdControl = null;
    List<Command> commands;
    @Getter
    private boolean isServer = true;
    @Getter
    private boolean global = false;
    @Getter
    private Collection<String> hosts = Collections.emptyList();
    private boolean announce = true;

    @Override
    public void onLoad() {
        saveDefaultConfig();
    }

    public void initCrowdControl() {
        reloadConfig();
        config = getConfig();
        String password = Objects.requireNonNullElseGet(manualPassword, () -> config.getString("password", ""));
        String ip = config.getString("ip", "127.0.0.1");

        if (!config.getBoolean("legacy", false)) {
            isServer = true;
            if (!password.isBlank()) {
                getLogger().info("Running Crowd Control in server mode");
                crowdControl = CrowdControl.server().port(PORT).password(password).build();
            } else {
                getLogger().severe("No password has been set in the plugin's config file. Please set one by editing plugins/CrowdControl/config.yml or set a temporary password using the /password command.");
                return;
            }
        } else {
            isServer = false;
            if (ip.isBlank())
                throw new IllegalStateException("IP address is blank. Please fix this in the config.yml file");
            getLogger().info("Running Crowd Control in client mode");
            crowdControl = CrowdControl.client().port(PORT).ip(ip).build();
        }

        if (commands == null)
            commands = RegisterCommands.register(this);
        else
            RegisterCommands.register(this, commands);
    }

    @Override
    public void updateCrowdControl(@Nullable CrowdControl crowdControl) {
        this.crowdControl = crowdControl;
    }

    @SneakyThrows
    @Override
    public void onEnable() {
        global = config.getBoolean("global", false);
        announce = config.getBoolean("announce", true);
        hosts = Collections.unmodifiableCollection(config.getStringList("hosts"));
        if (!hosts.isEmpty()) {
            Set<String> loweredHosts = new HashSet<>(hosts.size());
            for (String host : hosts)
                loweredHosts.add(host.toLowerCase(Locale.ENGLISH));
            hosts = Collections.unmodifiableSet(loweredHosts);
        }

        initCrowdControl();

        Bukkit.getPluginManager().registerEvents(this, this);

        commandManager = new PaperCommandManager<>(this,
                CommandExecutionCoordinator.simpleCoordinator(),
                Function.identity(),
                Function.identity()
        );
        registerChatCommands();
    }

    @Override
    public void onDisable() {
        if (crowdControl == null) return;
        crowdControl.shutdown("Plugin is unloading (server may be shutting down)");
        crowdControl = null;
        commands = null;
    }

    public boolean announceEffects() {
        return announce;
    }

    @CheckReturnValue
    @NotNull
    public List<@NotNull Player> getAllPlayers() {
        return playerMapper.getAllPlayers();
    }

    @CheckReturnValue
    @NotNull
    public List<@NotNull Player> getPlayers(final @NotNull Request request) {
        return playerMapper.getPlayers(request);
    }

    @Override
    public void registerCommand(@NotNull String name, dev.qixils.crowdcontrol.common.@NotNull Command<Player> command) {
        name = name.toLowerCase(Locale.ENGLISH);
        crowdControl.registerHandler(name, command::executeAndNotify);
        getLogger().fine("Registered CC command '" + name + "'");
    }

    @Override
    public @Nullable String getPassword() {
        if (!isServer()) return null;
        if (crowdControl != null)
            return crowdControl.getPassword(); // should be non-null because isServer is true
        if (manualPassword != null)
            return manualPassword;
        return config.getString("password");
    }

    @Override
    public void setPassword(@NotNull String password) throws IllegalArgumentException, IllegalStateException {
        if (!isServer())
            throw new IllegalStateException("Not running in server mode");
        manualPassword = password;
    }

    @Override
    public boolean isAdmin(@NotNull CommandSender commandSource) {
        return commandSource.hasPermission(ADMIN_PERMISSION) || commandSource.isOp();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        onPlayerJoin(event.getPlayer());
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
