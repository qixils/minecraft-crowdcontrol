package dev.qixils.crowdcontrol.plugin.paper;

import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import dev.qixils.crowdcontrol.CrowdControl;
import dev.qixils.crowdcontrol.common.*;
import dev.qixils.crowdcontrol.common.command.Command;
import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.common.mc.CCPlayer;
import dev.qixils.crowdcontrol.common.util.TextUtilImpl;
import dev.qixils.crowdcontrol.plugin.paper.mc.PaperPlayer;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
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
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

public final class PaperCrowdControlPlugin extends JavaPlugin implements Listener, Plugin<Player, CommandSender> {
	private static final Map<String, Boolean> VALID_SOUNDS = new HashMap<>();
	public static final PersistentDataType<Byte, Boolean> BOOLEAN_TYPE = new BooleanDataType();
	public static final PersistentDataType<String, Component> COMPONENT_TYPE = new ComponentDataType();
	@Getter
	private final Executor syncExecutor = runnable -> Bukkit.getScheduler().runTask(this, runnable);
	@Getter
	private final Executor asyncExecutor = runnable -> Bukkit.getScheduler().runTaskAsynchronously(this, runnable);
	@Getter
	@Accessors(fluent = true)
	private final PlayerEntityMapper<Player> playerMapper = new PlayerMapper<>(this);
	@Getter
	@Accessors(fluent = true)
	private final EntityMapper<CommandSender> commandSenderMapper = new CommandSenderMapper<>(this);
	private final SoftLockResolver softLockResolver = new SoftLockResolver(this);
	@Getter
	private final PaperPlayerManager playerManager = new PaperPlayerManager(this);
	@SuppressWarnings("deprecation") // ComponentFlattenerProvider has not been implemented yet
	@Getter
	private final TextUtilImpl textUtil = new TextUtilImpl(Bukkit.getUnsafe().componentFlattener());
	@Getter
	private final Class<Player> playerClass = Player.class;
	@Getter
	private final Class<CommandSender> commandSenderClass = CommandSender.class;
	FileConfiguration config = getConfig();
	// actual stuff
	String manualPassword = null; // set via /password
	@Getter
	CrowdControl crowdControl = null;
	@Getter
	private PaperCommandManager<CommandSender> commandManager;
	@Getter
	private boolean isServer = true;
	@Getter
	private boolean global = false;
	@Getter @NotNull
	private HideNames hideNames = HideNames.NONE;
	@Getter
	private Collection<String> hosts = Collections.emptyList();
	private boolean announce = true;
	@Getter
	private boolean adminRequired = false;
	@Getter
	private LimitConfig limitConfig = new LimitConfig();
	@Getter
	@Accessors(fluent = true)
	private final CommandRegister commandRegister = new CommandRegister(this);
	@Getter @NotNull
	private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

	@Override
	public void onLoad() {
		saveDefaultConfig();
		// init sound validator
		CommandConstants.SOUND_VALIDATOR = key -> {
			String asString = key.value();
			Boolean value = VALID_SOUNDS.get(asString);
			if (value != null)
				return value;

			try {
				Sound.valueOf(asString.toUpperCase(Locale.ENGLISH).replace('.', '_'));
				VALID_SOUNDS.put(asString, true);
				return true;
			} catch (IllegalArgumentException e) {
				VALID_SOUNDS.put(asString, false);
				return false;
			}
		};
	}

	public void initCrowdControl() {
		reloadConfig();
		config = getConfig();
		int port = config.getInt("port", DEFAULT_PORT);

		if (!config.getBoolean("legacy", false)) {
			isServer = true;
			String password = Objects.requireNonNullElseGet(manualPassword, () -> config.getString("password", ""));
			if (!password.isBlank()) {
				getLogger().info("Running Crowd Control in server mode");
				crowdControl = CrowdControl.server().port(port).password(password).build();
			} else {
				getLogger().severe("No password has been set in the plugin's config file. Please set one by editing plugins/CrowdControl/config.yml or set a temporary password using the /password command.");
				return;
			}
		} else {
			isServer = false;
			String ip = config.getString("ip", "127.0.0.1");
			if (ip.isBlank())
				throw new IllegalStateException("IP address is blank. Please fix this in the plugins/CrowdControl/config.yml file.");
			getLogger().info("Running Crowd Control in client mode");
			crowdControl = CrowdControl.client().port(port).ip(ip).build();
		}

		commandRegister().register();
		postInitCrowdControl(crowdControl);
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
		adminRequired = config.getBoolean("admin-required", false);
		hosts = Collections.unmodifiableCollection(config.getStringList("hosts"));
		hideNames = HideNames.fromConfigCode(config.getString("hide-names", "none"));
		if (!hosts.isEmpty()) {
			Set<String> loweredHosts = new HashSet<>(hosts.size());
			for (String host : hosts)
				loweredHosts.add(host.toLowerCase(Locale.ENGLISH));
			hosts = Collections.unmodifiableSet(loweredHosts);
		}

		// limit config
		boolean hostsBypass = config.getBoolean("limits.hosts-bypass", true);
		Map<String, Integer> itemLimits = config.getObject("limits.items", Map.class);
		Map<String, Integer> entityLimits = config.getObject("limits.entities", Map.class);
		limitConfig = new LimitConfig(hostsBypass, itemLimits, entityLimits);

		initCrowdControl();

		Bukkit.getPluginManager().registerEvents(this, this);
		Bukkit.getPluginManager().registerEvents(softLockResolver, this);

		try {
			commandManager = new PaperCommandManager<>(this,
					AsynchronousCommandExecutionCoordinator.<CommandSender>builder()
							.withAsynchronousParsing().build(),
					Function.identity(),
					Function.identity()
			);
			try {
				commandManager.registerBrigadier();
				commandManager.registerAsynchronousCompletions();
			} catch (Exception exception) {
				getSLF4JLogger().error("The command manager was unable to fully initialize. Please report this error to the developer.", exception);
			}
			registerChatCommands();
		} catch (Exception exception) {
			throw new IllegalStateException("The command manager was unable to load. Please ensure you are using the latest version of Paper.", exception);
		}
	}

	@Override
	public @NotNull Logger getSLF4JLogger() {
		return super.getSLF4JLogger();
	}

	@Override
	public void onDisable() {
		if (crowdControl != null) {
			crowdControl.shutdown("Plugin is unloading (server may be shutting down)");
			crowdControl = null;
		}
	}

	public boolean announceEffects() {
		return announce;
	}

	@Override
	public void registerCommand(@NotNull String name, @NotNull Command<Player> command) {
		name = name.toLowerCase(Locale.ENGLISH);
		try {
			crowdControl.registerHandler(name, command::executeAndNotify);
			getLogger().fine("Registered CC command '" + name + "'");
		} catch (IllegalArgumentException e) {
			getSLF4JLogger().warn("Failed to register command: " + name, e);
		}
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
	public @NotNull Audience getConsole() {
		return Bukkit.getConsoleSender();
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		onPlayerJoin(event.getPlayer());
	}

	@Override
	public @NotNull CCPlayer getPlayer(@NotNull Player player) {
		return new PaperPlayer(player);
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

	private static final class ComponentDataType implements PersistentDataType<String, Component> {
		private final GsonComponentSerializer serializer = GsonComponentSerializer.gson();

		@Override
		public @NotNull Class<String> getPrimitiveType() {
			return String.class;
		}

		@Override
		public @NotNull Class<Component> getComplexType() {
			return Component.class;
		}

		@Override
		public @NotNull String toPrimitive(@NotNull Component complex, @NotNull PersistentDataAdapterContext context) {
			return serializer.serialize(complex);
		}

		@Override
		public @NotNull Component fromPrimitive(@NotNull String primitive, @NotNull PersistentDataAdapterContext context) {
			return serializer.deserialize(primitive);
		}
	}
}
