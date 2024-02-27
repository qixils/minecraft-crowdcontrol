package dev.qixils.crowdcontrol.plugin.paper;

import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import dev.qixils.crowdcontrol.CrowdControl;
import dev.qixils.crowdcontrol.common.*;
import dev.qixils.crowdcontrol.common.command.Command;
import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.common.mc.CCPlayer;
import dev.qixils.crowdcontrol.common.util.SemVer;
import dev.qixils.crowdcontrol.common.util.TextUtilImpl;
import dev.qixils.crowdcontrol.plugin.paper.mc.PaperPlayer;
import dev.qixils.crowdcontrol.plugin.paper.utils.ReflectionUtil;
import dev.qixils.crowdcontrol.socket.SocketManager;
import io.papermc.lib.PaperLib;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

import static dev.qixils.crowdcontrol.common.SoftLockConfig.*;
import static dev.qixils.crowdcontrol.plugin.paper.utils.ReflectionUtil.*;

public final class PaperCrowdControlPlugin extends JavaPlugin implements Listener, Plugin<Player, CommandSender> {
	public static final @NotNull ComponentLogger LOGGER = ComponentLogger.logger("CrowdControl/Plugin");
	public static final @NotNull SemVer MINECRAFT_VERSION = new SemVer(Bukkit.getMinecraftVersion());
	public static final @NotNull Set<SemVer> MAPPED_VERSIONS = Set.of(
		new SemVer(1, 19, 4),
		new SemVer(1, 20),
		new SemVer(1, 20, 1),
		new SemVer(1, 20, 2),
		new SemVer(1, 20, 3),
		new SemVer(1, 20, 4)
	);
	private static final Map<String, Boolean> VALID_SOUNDS = new HashMap<>();
	public static final PersistentDataType<Byte, Boolean> BOOLEAN_TYPE = new BooleanDataType();
	public static final PersistentDataType<String, Component> COMPONENT_TYPE = new ComponentDataType();
	@Getter
	private final Executor syncExecutor = runnable -> Bukkit.getScheduler().runTask(this, runnable);
	@Getter
	private final Executor asyncExecutor = runnable -> Bukkit.getScheduler().runTaskAsynchronously(this, runnable);
	@Getter
	@Accessors(fluent = true)
	private final PlayerEntityMapper<Player> playerMapper = new PlayerMapper(this);
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
	@Getter @Setter
	private @Nullable String password = DEFAULT_PASSWORD;
	@Getter @Setter
	private String IP = null;
	@Getter @Setter
	private int port = DEFAULT_PORT;
	@Getter
	CrowdControl crowdControl = null;
	@Getter
	private PaperCommandManager<CommandSender> commandManager;
	@Getter
	private boolean global = false;
	@Getter @Setter @NotNull
	private HideNames hideNames = HideNames.NONE;
	@Getter
	private Collection<String> hosts = Collections.emptyList();
	private boolean announce = true;
	@Getter
	private boolean adminRequired = false;
	@Getter
	private boolean autoDetectIP = true;
	@Getter
	private LimitConfig limitConfig = new LimitConfig();
	@Getter @NotNull
	private SoftLockConfig softLockConfig = new SoftLockConfig();
	@Getter
	@Accessors(fluent = true)
	private final CommandRegister commandRegister = new CommandRegister(this);
	@Getter @NotNull
	private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
	@Getter @NotNull
	private final Map<String, List<SocketManager>> sentEvents = new HashMap<>();

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

	@Override
	public void loadConfig() {
		reloadConfig();
		config = getConfig();

		// soft-lock observer
		ConfigurationSection softLockSection = config.getConfigurationSection("soft-lock-observer");
		if (softLockSection == null) {
			getSLF4JLogger().debug("No soft-lock config found, using defaults");
			softLockConfig = new SoftLockConfig();
		} else {
			getSLF4JLogger().debug("Loading soft-lock config");
			softLockConfig = new SoftLockConfig(
				softLockSection.getInt("period", DEF_PERIOD),
				softLockSection.getInt("deaths", DEF_DEATHS),
				config.getInt("search-horizontal", DEF_SEARCH_HORIZ),
				config.getInt("search-vertical", DEF_SEARCH_VERT)
			);
		}

		// hosts
		hosts = Collections.unmodifiableCollection(config.getStringList("hosts"));
		if (!hosts.isEmpty()) {
			Set<String> loweredHosts = new HashSet<>(hosts.size());
			for (String host : hosts)
				loweredHosts.add(host.toLowerCase(Locale.ENGLISH));
			hosts = Collections.unmodifiableSet(loweredHosts);
		}

		// limit config
		ConfigurationSection limitSection = config.getConfigurationSection("limits");
		if (limitSection == null) {
			getSLF4JLogger().debug("No limit config found, using defaults");
			limitConfig = new LimitConfig();
		} else {
			getSLF4JLogger().debug("Loading limit config");
			boolean hostsBypass = limitSection.getBoolean("hosts-bypass", true);
			Map<String, Integer> itemLimits = parseLimitConfigSection(limitSection.getConfigurationSection("items"));
			Map<String, Integer> entityLimits = parseLimitConfigSection(limitSection.getConfigurationSection("entities"));
			limitConfig = new LimitConfig(hostsBypass, itemLimits, entityLimits);
		}

		// misc
		global = config.getBoolean("global", global);
		announce = config.getBoolean("announce", announce);
		adminRequired = config.getBoolean("admin-required", adminRequired);
		hideNames = HideNames.fromConfigCode(config.getString("hide-names", hideNames.getConfigCode()));
		port = config.getInt("port", port);
		IP = config.getString("ip", IP);
		password = config.getString("password", password);
		autoDetectIP = config.getBoolean("ip-detect", autoDetectIP);
	}

	public void initCrowdControl() {
		loadConfig();

		if (password == null || password.isEmpty()) { // TODO: allow empty password if CC allows it
			getLogger().severe("No password has been set in the plugin's config file. Please set one by editing plugins/CrowdControl/config.yml or set a temporary password using the /password command.");
			return;
		}
		crowdControl = CrowdControl.server().ip(IP).port(port).password(password).build();

		commandRegister().register();
		postInitCrowdControl(crowdControl);
	}

	@Override
	public void updateCrowdControl(@Nullable CrowdControl crowdControl) {
		this.crowdControl = crowdControl;
	}

	@Contract("null -> null; !null -> !null")
	private static Map<String, Integer> parseLimitConfigSection(@Nullable ConfigurationSection section) {
		if (section == null)
			return null;
		Set<String> keys = section.getKeys(false);
		Map<String, Integer> map = new HashMap<>(keys.size());
		for (String key : keys) {
			map.put(key, section.getInt(key));
		}
		return map;
	}

	@SneakyThrows
	@Override
	public void onEnable() {
		if (!PaperLib.isPaper()) {
			throw new IllegalStateException("The Paper server software is required. Please upgrade from Spigot to Paper, it should be a simple and painless upgrade in 99.99% of cases.");
		}
		if (MINECRAFT_VERSION.isLessThan(new SemVer(1, 19, 4))) {
			throw new IllegalStateException("Versions prior to 1.19.4 are no longer supported.");
		}
		if (!MAPPED_VERSIONS.contains(MINECRAFT_VERSION)) {
			getLogger().warning("This version of Crowd Control has not been confirmed to work with the current version of Minecraft. Please check for updates to the plugin.");
		}

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
				getSLF4JLogger().warn("Chat command manager partially failed to initialize, ignoring.");
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
	public void setAnnounceEffects(boolean announceEffects) {
		announce = announceEffects;
	}

	@Override
	public void registerCommand(@Nullable String name, @NotNull Command<Player> command) {
		if (name != null)
			name = name.toLowerCase(Locale.ENGLISH);
		try {
			crowdControl.registerHandler(name, command::executeAndNotify);
			getLogger().fine("Registered CC command '" + name + "'");
		} catch (IllegalArgumentException e) {
			getSLF4JLogger().warn("Failed to register command: " + name, e);
		}
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

	public static boolean isFeatureEnabled(Object feature) {
		try {
			Optional<Object> requiredFeaturesOpt;
			if (feature instanceof FeatureElementCommand command) {
				requiredFeaturesOpt = command.requiredFeatures();
			} else if (isInstance(FEATURE_ELEMENT_CLAZZ, feature)) {
				requiredFeaturesOpt = ReflectionUtil.invokeMethod(
						feature,
						// FeatureElement#requiredFeatures
						switch (Bukkit.getMinecraftVersion()) {
							case "1.19.4", "1.20", "1.20.1", "1.20.2", "1.20.3", "1.20.4" -> "m";
							default -> throw new IllegalStateException();
						}
				);
			} else if (isInstance(FEATURE_FLAG_SET_CLAZZ, feature)) {
				requiredFeaturesOpt = Optional.of(feature);
			} else {
				LOGGER.warn("Unknown feature type: " + feature.getClass().getName());
				return true;
			}
			return requiredFeaturesOpt.flatMap(requiredFeatures -> ReflectionUtil.invokeMethod(
					Bukkit.getServer(),
					// CraftServer#getServer
					"getServer"
			).flatMap(server -> ReflectionUtil.invokeMethod(
					server,
					// MinecraftServer#getWorldData
					switch (Bukkit.getMinecraftVersion()) {
						case "1.19.4" -> "aW";
						case "1.20", "1.20.1" -> "aU";
						case "1.20.2" -> "aT";
						case "1.20.3", "1.20.4" -> "aY";
						default -> throw new IllegalStateException();
					}
			)).flatMap(worldData -> ReflectionUtil.invokeMethod(
					worldData,
					// WorldData#enabledFeatures
					switch (Bukkit.getMinecraftVersion()) {
						case "1.19.4" -> "L";
						case "1.20", "1.20.1", "1.20.2", "1.20.3", "1.20.4" -> "M";
						default -> throw new IllegalStateException();
					}
			)).<Boolean>flatMap(enabledFeatures -> ReflectionUtil.invokeMethod(
					requiredFeatures,
					// FeatureFlagSet#isSubsetOf
					switch (Bukkit.getMinecraftVersion()) {
						case "1.19.4", "1.20", "1.20.1", "1.20.2", "1.20.3", "1.20.4" -> "a";
						default -> throw new IllegalStateException();
					},
					enabledFeatures
			))).orElse(true);
		} catch (Exception e) {
			return true;
		}
	}

	public static boolean isFeatureDisabled(Object feature) {
		return !isFeatureEnabled(feature);
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
