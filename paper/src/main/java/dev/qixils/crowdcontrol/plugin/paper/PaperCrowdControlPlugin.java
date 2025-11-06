package dev.qixils.crowdcontrol.plugin.paper;

import dev.qixils.crowdcontrol.common.*;
import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.common.components.MovementStatusValue;
import dev.qixils.crowdcontrol.common.mc.MCCCPlayer;
import dev.qixils.crowdcontrol.common.packets.*;
import dev.qixils.crowdcontrol.common.packets.util.ExtraFeature;
import dev.qixils.crowdcontrol.common.util.SemVer;
import dev.qixils.crowdcontrol.common.util.TextUtilImpl;
import dev.qixils.crowdcontrol.plugin.paper.mc.PaperPlayer;
import dev.qixils.crowdcontrol.plugin.paper.utils.PaperUtil;
import io.papermc.lib.PaperLib;
import io.papermc.paper.ServerBuildInfo;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Executor;

import static dev.qixils.crowdcontrol.common.SoftLockConfig.*;

@SuppressWarnings("UnstableApiUsage")
public final class PaperCrowdControlPlugin extends Plugin<Player, CommandSourceStack> implements Listener {
	public static final @NotNull ComponentLogger LOGGER = ComponentLogger.logger("CrowdControl/Plugin");
	public static final @NotNull SemVer MINECRAFT_MIN_VERSION = new SemVer(1, 20, 6);
	public static final @NotNull SemVer MINECRAFT_VERSION = new SemVer(Bukkit.getMinecraftVersion());
	private static final Map<String, Boolean> VALID_SOUNDS = new HashMap<>();
	public static final PersistentDataType<Byte, Boolean> BOOLEAN_TYPE = new BooleanDataType();
	public static final PersistentDataType<String, Component> COMPONENT_TYPE = new ComponentDataType();
	public static final PersistentDataType<String, MovementStatusValue> MOVEMENT_STATUS_VALUE_TYPE = new EnumDataType<>(MovementStatusValue.class, o -> o.orElse(MovementStatusValue.ALLOWED));

	@Getter
	private final Executor syncExecutor;
	@Getter
	private final Executor asyncExecutor;
	@Getter
	private final Path dataFolder;
	@Getter
	@Accessors(fluent = true)
	private final PlayerEntityMapper<Player> playerMapper = new PlayerMapper(this);
	@Getter
	@Accessors(fluent = true)
	private final EntityMapper<CommandSourceStack> commandSenderMapper = new CommandSourceStackMapper(new CommandSenderMapper<>(this));
	private final SoftLockResolver softLockResolver = new SoftLockResolver(this);
	@Getter
	private final PaperPlayerManager playerManager = new PaperPlayerManager(this);
	@SuppressWarnings("deprecation") // ComponentFlattenerProvider has not been implemented yet
	@Getter
	private final TextUtilImpl textUtil = new TextUtilImpl(Bukkit.getUnsafe().componentFlattener());
	@Getter
	private final PaperLoader paperPlugin;
	// actual stuff
	@Getter
	private PaperCommandManager<CommandSourceStack> commandManager;
	@Getter
	@Accessors(fluent = true)
	private final CommandRegister commandRegister = new CommandRegister(this);
	@Getter
	@NotNull
	private final PluginChannel pluginChannel = new PluginChannel(this);

	public PaperCrowdControlPlugin(@NotNull PaperLoader paperPlugin) {
		super(Player.class, CommandSourceStack.class);
		this.dataFolder = paperPlugin.getDataFolder().toPath().resolve("Data");
		this.paperPlugin = paperPlugin;
		syncExecutor = runnable -> Bukkit.getGlobalRegionScheduler().execute(paperPlugin, runnable);
		asyncExecutor = runnable -> Bukkit.getAsyncScheduler().runNow(paperPlugin, $ -> runnable.run());
	}

	public void onLoad() {
		paperPlugin.saveDefaultConfig();

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
			updateConditionalEffectVisibility(player);
		});
		pluginChannel.registerIncomingPluginChannel(ExtraFeaturePacketC2S.METADATA, (player, message) -> {
			UUID uuid = player.getUniqueId();
			Set<ExtraFeature> features = message.features();
			getSLF4JLogger().info("Received features {} from client {}", features, uuid);
			extraFeatures.put(uuid, features);
			updateConditionalEffectVisibility(player);
		});
	}

	@Override
	public void loadConfig() {
		paperPlugin.reloadConfig();
		FileConfiguration config = paperPlugin.getConfig();

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
				softLockSection.getInt("search-horizontal", DEF_SEARCH_HORIZ),
				softLockSection.getInt("search-vertical", DEF_SEARCH_VERT)
			);
		}

		// custom effects
		ConfigurationSection customEffectsSection = config.getConfigurationSection("custom-effects");
		if (customEffectsSection == null) {
			LOGGER.debug("No custom effects config found, using defaults");
			customEffectsConfig = new CustomEffectsConfig();
		} else {
			LOGGER.debug("Loading custom effects config");
			customEffectsConfig = new CustomEffectsConfig(
				customEffectsSection.getBoolean("enabled", false)
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
	public void onEnable() {
		if (!PaperLib.isPaper()) {
			throw new IllegalStateException("The Paper server software is required. Please upgrade from Spigot to Paper, it should be a simple and painless upgrade in 99.99% of cases.");
		}
		if (MINECRAFT_VERSION.isLessThan(MINECRAFT_MIN_VERSION)) {
			throw new IllegalStateException("Versions prior to " + MINECRAFT_MIN_VERSION + " are no longer supported.");
		}

		initCrowdControl();

		Bukkit.getPluginManager().registerEvents(this, paperPlugin);
		Bukkit.getPluginManager().registerEvents(softLockResolver, paperPlugin);

		try {
			commandManager = PaperCommandManager.builder()
				.executionCoordinator(ExecutionCoordinator.asyncCoordinator())
				.buildOnEnable(paperPlugin);
			registerChatCommands();
		} catch (Exception exception) {
			throw new IllegalStateException("The command manager was unable to load. Please ensure you are using the latest version of Paper.", exception);
		}
	}

	@Override
	public @NotNull ComponentLogger getSLF4JLogger() {
		return LOGGER;
	}

	public void onDisable() {
		shutdown();
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
			player.getScheduler().execute(paperPlugin, () -> pluginChannel.sendMessage(player, VersionRequestPacketS2C.INSTANCE), null, 10);
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
	public @NotNull MCCCPlayer getPlayer(@NotNull Player player) {
		return new PaperPlayer(this, player);
	}

	@Override
	public @Nullable Player asPlayer(@NotNull CommandSourceStack sender) {
		return objAsPlayer(sender.getSender());
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
