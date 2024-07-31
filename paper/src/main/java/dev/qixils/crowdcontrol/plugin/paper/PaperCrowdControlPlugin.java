package dev.qixils.crowdcontrol.plugin.paper;

import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import dev.qixils.crowdcontrol.CrowdControl;
import dev.qixils.crowdcontrol.common.*;
import dev.qixils.crowdcontrol.common.command.Command;
import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.common.components.MovementStatusValue;
import dev.qixils.crowdcontrol.common.mc.CCPlayer;
import dev.qixils.crowdcontrol.common.packets.*;
import dev.qixils.crowdcontrol.common.packets.util.ExtraFeature;
import dev.qixils.crowdcontrol.common.util.SemVer;
import dev.qixils.crowdcontrol.common.util.TextUtilImpl;
import dev.qixils.crowdcontrol.plugin.paper.mc.PaperPlayer;
import dev.qixils.crowdcontrol.plugin.paper.utils.PaperUtil;
import dev.qixils.crowdcontrol.socket.SocketManager;
import io.papermc.lib.PaperLib;
import io.papermc.paper.ServerBuildInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

import static dev.qixils.crowdcontrol.common.SoftLockConfig.*;

public final class PaperCrowdControlPlugin extends JavaPlugin implements Listener, Plugin<Player, CommandSender> {
	public static final @NotNull ComponentLogger LOGGER = ComponentLogger.logger("CrowdControl/Plugin");
	public static final @NotNull SemVer MINECRAFT_MIN_VERSION = new SemVer(1, 20, 6);
	public static final @NotNull SemVer MINECRAFT_VERSION = new SemVer(Bukkit.getMinecraftVersion());
	private static final Map<String, Boolean> VALID_SOUNDS = new HashMap<>();
	public static final PersistentDataType<Byte, Boolean> BOOLEAN_TYPE = new BooleanDataType();
	public static final PersistentDataType<String, Component> COMPONENT_TYPE = new ComponentDataType();
	public static final PersistentDataType<String, MovementStatusValue> MOVEMENT_STATUS_VALUE_TYPE = new EnumDataType<>(MovementStatusValue.class, o -> o.orElse(MovementStatusValue.ALLOWED));

	@Getter
	private final Executor syncExecutor = runnable -> Bukkit.getGlobalRegionScheduler().execute(this, runnable);
	@Getter
	private final Executor asyncExecutor = runnable -> Bukkit.getAsyncScheduler().runNow(this, $ -> runnable.run());
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
	@Getter
	@Setter
	private @Nullable String password = DEFAULT_PASSWORD;
	@Getter
	@Setter
	private String IP = null;
	@Getter
	@Setter
	private int port = DEFAULT_PORT;
	@Getter
	CrowdControl crowdControl = null;
	@Getter
	private PaperCommandManager<CommandSender> commandManager;
	@Getter
	private boolean global = false;
	@Getter
	@Setter
	@NotNull
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
	@NotNull
	private SoftLockConfig softLockConfig = new SoftLockConfig();
	@Getter
	@Accessors(fluent = true)
	private final CommandRegister commandRegister = new CommandRegister(this);
	@Getter
	@NotNull
	private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
	@Getter
	@NotNull
	private final Map<String, List<SocketManager>> sentEvents = new HashMap<>();
	@Getter
	@NotNull
	private final PluginChannel pluginChannel = new PluginChannel(this);
	private final Map<UUID, SemVer> clientVersions = new HashMap<>();
	private final Map<UUID, Set<ExtraFeature>> extraFeatures = new HashMap<>();

	@Override
	public void onLoad() {
		saveDefaultConfig();

		// init sound validator
		CommandConstants.SOUND_VALIDATOR = _key -> {
			NamespacedKey key = PaperUtil.toPaper(_key);
			String asString = key.asMinimalString();
			Boolean value = VALID_SOUNDS.get(asString);
			if (value != null)
				return value;

			value = Registry.SOUNDS.get(key) != null;
			VALID_SOUNDS.put(asString, value);
			return value;
		};

		// init plugin channels
		pluginChannel.registerOutgoingPluginChannel(VersionRequestPacketS2C.METADATA);
		pluginChannel.registerOutgoingPluginChannel(ShaderPacketS2C.METADATA);
		pluginChannel.registerOutgoingPluginChannel(MovementStatusPacketS2C.METADATA);
		pluginChannel.registerOutgoingPluginChannel(SetLanguagePacketS2C.METADATA);
		pluginChannel.registerIncomingPluginChannel(VersionResponsePacketC2S.METADATA, (player, message) -> {
			UUID uuid = player.getUniqueId();
			SemVer version = message.version();
			getSLF4JLogger().info("Received version {} from client {}", version, uuid);
			clientVersions.put(uuid, version);
			updateConditionalEffectVisibility(crowdControl);
		});
		pluginChannel.registerIncomingPluginChannel(ExtraFeaturePacketC2S.METADATA, (player, message) -> {
			UUID uuid = player.getUniqueId();
			Set<ExtraFeature> features = message.features();
			getSLF4JLogger().info("Received features {} from client {}", features, uuid);
			extraFeatures.put(uuid, features);
			updateConditionalEffectVisibility(crowdControl);
		});
	}

	@Override
	public void loadConfig() {
		reloadConfig();
		config = getConfig();

		// soft-lock observer
		ConfigurationSection softLockSection = config.getConfigurationSection("soft-lock-observer");
		if (softLockSection == null) {
			LOGGER.debug("No soft-lock config found, using defaults");
			softLockConfig = new SoftLockConfig();
		} else {
			LOGGER.debug("Loading soft-lock config");
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
			LOGGER.debug("No limit config found, using defaults");
			limitConfig = new LimitConfig();
		} else {
			LOGGER.debug("Loading limit config");
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
		if ("".equals(IP) || "null".equalsIgnoreCase(IP) || "127.0.0.1".equals(IP)) IP = null;
		password = config.getString("password", password);
		autoDetectIP = config.getBoolean("ip-detect", autoDetectIP);
	}

	public void initCrowdControl() {
		loadConfig();

		if (password == null || password.isEmpty()) { // TODO: allow empty password if CC allows it
			LOGGER.error("No password has been set in the plugin's config file. Please set one by editing plugins/CrowdControl/config.yml or set a temporary password using the /password command.");
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
		if (MINECRAFT_VERSION.isLessThan(MINECRAFT_MIN_VERSION)) {
			throw new IllegalStateException("Versions prior to " + MINECRAFT_MIN_VERSION + " are no longer supported.");
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
				LOGGER.warn("Chat command manager partially failed to initialize, ignoring.");
			}
			registerChatCommands();
		} catch (Exception exception) {
			throw new IllegalStateException("The command manager was unable to load. Please ensure you are using the latest version of Paper.", exception);
		}
	}

	@Override
	public @NotNull ComponentLogger getSLF4JLogger() {
		return LOGGER;
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
			LOGGER.debug("Registered CC command '{}'", name);
		} catch (IllegalArgumentException e) {
			LOGGER.warn("Failed to register command: {}", name, e);
		}
	}

	@Override
	public @NotNull Audience getConsole() {
		return Bukkit.getConsoleSender();
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		onPlayerJoin(player);
		// Spigot docs allege you have to add a delay before sending plugin channel messages
		// This seems true, so we add a 5 tick delay, but ideally this is short enough to come before the effect visibility update
		if (!clientVersions.containsKey(player.getUniqueId())) {
			player.getScheduler().execute(this, () -> pluginChannel.sendMessage(player, VersionRequestPacketS2C.INSTANCE), null, 10);
		}
	}

	@EventHandler
	public void onLeave(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		clientVersions.remove(uuid);
		extraFeatures.remove(uuid);
		onPlayerLeave(event.getPlayer());
	}

	@Override
	public @NotNull CCPlayer getPlayer(@NotNull Player player) {
		return new PaperPlayer(this, player);
	}

	@Override
	public @NotNull Optional<SemVer> getModVersion(@NotNull Player player) {
		return Optional.ofNullable(clientVersions.get(player.getUniqueId()));
	}

	@Override
	public @NotNull Set<ExtraFeature> getExtraFeatures(@NotNull Player player) {
		return extraFeatures.get(player.getUniqueId());
	}

	@Override
	public int getModdedPlayerCount() {
		return clientVersions.size();
	}

	@Override
	public boolean isFeatureAvailable(@NotNull ExtraFeature feature) {
		return extraFeatures.values().stream().anyMatch(set -> set.contains(feature));
	}

	@Override
	public @NotNull VersionMetadata getVersionMetadata() {
		OptionalInt build = ServerBuildInfo.buildInfo().buildNumber();
		return new VersionMetadata(
			ServerBuildInfo.buildInfo().minecraftVersionId(),
			"Paper",
			ServerBuildInfo.buildInfo().brandName(),
			build.isPresent() ? Integer.toString(build.getAsInt()) : null
		);
	}

	public static boolean isFeatureEnabled(FeatureElementCommand feature) {
		return Bukkit.getWorlds().stream().allMatch(feature::isFeatureEnabled);
	}

	public static boolean isFeatureEnabled(Material material) {
		return Bukkit.getWorlds().stream().allMatch(material::isEnabledByFeature);
	}

	public static boolean isFeatureEnabled(EntityType entityType) {
		return Bukkit.getWorlds().stream().allMatch(entityType::isEnabledByFeature);
	}
}
