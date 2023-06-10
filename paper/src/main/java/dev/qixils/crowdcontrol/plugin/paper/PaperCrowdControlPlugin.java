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
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
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

import static dev.qixils.crowdcontrol.plugin.paper.utils.ReflectionUtil.*;

public final class PaperCrowdControlPlugin extends JavaPlugin implements Listener, Plugin<Player, CommandSender> {
	public static final @NotNull SemVer MINECRAFT_VERSION = new SemVer(Bukkit.getMinecraftVersion());
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
	private int port = DEFAULT_PORT;
	@Getter @Setter
	private @Nullable String IP = "127.0.0.1";
	@Getter
	CrowdControl crowdControl = null;
	@Getter
	private PaperCommandManager<CommandSender> commandManager;
	@Getter
	private boolean isServer = true;
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

	@Override
	public void loadConfig() {
		reloadConfig();
		config = getConfig();

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
		isServer = !config.getBoolean("legacy", !isServer);
		port = config.getInt("port", port);
		IP = config.getString("ip", IP);
		password = config.getString("password", password);
		autoDetectIP = config.getBoolean("autodetect", autoDetectIP);
	}

	public void initCrowdControl() {
		loadConfig();

		if (isServer) {
			getLogger().info("Running Crowd Control in server mode");
			if (password == null || password.isEmpty()) {
				getLogger().severe("No password has been set in the plugin's config file. Please set one by editing plugins/CrowdControl/config.yml or set a temporary password using the /password command.");
				return;
			}
			crowdControl = CrowdControl.server().port(port).password(password).build();
		} else {
			getLogger().info("Running Crowd Control in client mode");
			if (IP == null || IP.isEmpty()) {
				getLogger().severe("No IP address has been set in the plugin's config file. Please set one by editing plugins/CrowdControl/config.yml");
				return;
			}
			crowdControl = CrowdControl.client().port(port).ip(IP).build();
		}

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
						switch (Bukkit.getMinecraftVersion()) {
							case "1.19.4", "1.20" -> "m";
							default -> throw new IllegalStateException();
						}
				);
			} else if (isInstance(FEATURE_FLAG_SET_CLAZZ, feature)) {
				requiredFeaturesOpt = Optional.of(feature);
			} else {
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
						case "1.20" -> "aU";
						default -> throw new IllegalStateException();
					}
			)).flatMap(worldData -> ReflectionUtil.invokeMethod(
					worldData,
					// WorldData#enabledFeatures
					switch (Bukkit.getMinecraftVersion()) {
						case "1.19.4" -> "L";
						case "1.20" -> "M";
						default -> throw new IllegalStateException();
					}
			)).<Boolean>flatMap(enabledFeatures -> ReflectionUtil.invokeMethod(
					requiredFeatures,
					// FeatureFlagSet#isSubsetOf
					switch (Bukkit.getMinecraftVersion()) {
						case "1.19.4", "1.20" -> "a";
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
