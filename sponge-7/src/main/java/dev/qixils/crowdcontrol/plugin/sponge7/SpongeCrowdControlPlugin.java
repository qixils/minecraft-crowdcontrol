package dev.qixils.crowdcontrol.plugin.sponge7;

import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.sponge7.SpongeCommandManager;
import com.flowpowered.math.vector.Vector3d;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import dev.qixils.crowdcontrol.CrowdControl;
import dev.qixils.crowdcontrol.common.AbstractPlugin;
import dev.qixils.crowdcontrol.common.EntityMapper;
import dev.qixils.crowdcontrol.common.HideNames;
import dev.qixils.crowdcontrol.common.LimitConfig;
import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.common.mc.CCPlayer;
import dev.qixils.crowdcontrol.plugin.sponge7.data.entity.*;
import dev.qixils.crowdcontrol.plugin.sponge7.mc.SpongePlayer;
import dev.qixils.crowdcontrol.plugin.sponge7.utils.SpongeTextUtil;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.spongeapi.SpongeAudiences;
import net.kyori.adventure.text.serializer.spongeapi.SpongeComponentSerializer;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.Platform;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.asset.AssetId;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.property.block.MatterProperty;
import org.spongepowered.api.data.property.block.MatterProperty.Matter;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameRegistryEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.AsynchronousExecutor;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.scheduler.SynchronousExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.generator.dummy.DummyObjectProvider;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

import static net.kyori.adventure.key.Key.MINECRAFT_NAMESPACE;

@Plugin(
		id = "crowd-control",
		name = "Crowd Control",
		version = "3.3.0",
		description = "Allows viewers to interact with your Minecraft world",
		url = "https://github.com/qixils/minecraft-crowdcontrol",
		authors = {"qixils"}
)
@Getter
public class SpongeCrowdControlPlugin extends AbstractPlugin<Player, CommandSource> {
	// keys (though they don't really work)
	public static Key<Value<Text>> ORIGINAL_DISPLAY_NAME = DummyObjectProvider.createExtendedFor(Key.class, "ORIGINAL_DISPLAY_NAME");
	public static Key<Value<Boolean>> VIEWER_SPAWNED = DummyObjectProvider.createExtendedFor(Key.class, "VIEWER_SPAWNED");
	public static Key<Value<GameMode>> GAME_MODE_EFFECT = DummyObjectProvider.createExtendedFor(Key.class, "GAME_MODE_EFFECT");
	// "real" variables
	private final SoftLockResolver softLockResolver = new SoftLockResolver(this);
	@Accessors(fluent = true)
	private final CommandRegister commandRegister = new CommandRegister(this);
	private final SpongeTextUtil textUtil = new SpongeTextUtil();
	@Accessors(fluent = true)
	private final EntityMapper<CommandSource> commandSenderMapper = new CommandSourceMapper<>(this);
	@Accessors(fluent = true)
	private final EntityMapper<Player> playerMapper = new CommandSourceMapper<>(this);
	private final SpongePlayerManager playerManager = new SpongePlayerManager(this);
	private SpongeCommandManager<CommandSource> commandManager;
	private ConfigurationLoader<CommentedConfigurationNode> configLoader;
	private Scheduler scheduler;
	private SpongeComponentSerializer spongeSerializer;
	private String clientHost = null;
	// injected variables
	@Inject
	private Logger logger;
	@Inject
	private PluginContainer pluginContainer;
	@Inject
	@SynchronousExecutor
	private SpongeExecutorService syncExecutor;
	@Inject
	@AsynchronousExecutor
	private SpongeExecutorService asyncExecutor;
	@Inject
	private Game game;
	@Inject
	@AssetId("default.conf")
	private Asset defaultConfig;
	@Inject
	@DefaultConfig(sharedRoot = true)
	private Path configPath;
	@Inject
	private SpongeAudiences audiences;
	@Inject
	private GameRegistry registry;
	// registries
	private DataRegistration<OriginalDisplayNameData, ImmutableOriginalDisplayNameData> ORIGINAL_DISPLAY_NAME_DATA_REGISTRATION;
	private DataRegistration<ViewerSpawnedData, ImmutableViewerSpawnedData> VIEWER_SPAWNED_DATA_REGISTRATION;
	private DataRegistration<GameModeEffectData, ImmutableGameModeEffectData> GAME_MODE_EFFECT_DATA_REGISTRATION;

	public SpongeCrowdControlPlugin() {
		super(Player.class, CommandSource.class);
	}

	public static void spawnPlayerParticles(Entity entity, ParticleEffect particle) {
		World world = entity.getWorld();
		Location<World> location = entity.getLocation();
		Vector3d position = new Vector3d(location.getX(), Math.ceil(location.getY()), location.getZ());
		world.spawnParticles(particle, position, 75);
	}

	public static void spawnPlayerParticles(Entity entity, ParticleType particle, int count) {
		spawnPlayerParticles(
				entity,
				ParticleEffect.builder()
						.type(particle)
						.quantity(count)
						.build()
		);
	}

	public static net.kyori.adventure.key.Key key(final CatalogType catalogType) {
		return net.kyori.adventure.key.Key.key(catalogType.getId());
	}

	public static boolean isMatter(BlockState block, Matter matter) {
		Optional<MatterProperty> matterProp = block.getProperty(MatterProperty.class);
		return matterProp.isPresent() && matter.equals(matterProp.get().getValue());
	}

	public static boolean isLiquid(BlockState block) {
		return isMatter(block, Matter.LIQUID);
	}

	public @NotNull SpongeAudiences adventure() {
		if (audiences == null)
			throw new IllegalStateException("Tried to access adventure before plugin loaded");
		return audiences;
	}

	@Override
	public @NotNull Audience getConsole() {
		return adventure().console();
	}

	@NotNull
	public Audience asAudience(@NotNull CommandSource source) {
		return commandSenderMapper().asAudience(source);
	}

	@NotNull
	public Audience asAudience(@NotNull World world) {
		return adventure().world(net.kyori.adventure.key.Key.key(MINECRAFT_NAMESPACE, world.getName()));
	}

	@Override
	public @NotNull Logger getSLF4JLogger() {
		return logger;
	}

	@Override
	public @NotNull Collection<String> getHosts() {
		Collection<String> confHosts = super.getHosts();
		if (clientHost == null)
			return confHosts;
		Set<String> hosts = new HashSet<>(confHosts.size() + 1);
		hosts.addAll(confHosts);
		hosts.add(clientHost);
		return hosts;
	}

	@Override
	public @Nullable String getPassword() {
		if (!isServer()) return null;
		if (crowdControl != null)
			return crowdControl.getPassword();
		if (manualPassword != null)
			return manualPassword;
		try {
			return configLoader.load().getNode("password").getString();
		} catch (IOException e) {
			logger.warn("Could not load config", e);
			return null;
		}
	}

	@Listener
	public void onKeyRegistration(GameRegistryEvent.Register<Key<?>> event) {
		ORIGINAL_DISPLAY_NAME = Key.builder()
				.type(new TypeToken<Value<Text>>() {
				})
				.id("original_display_name")
				.name("Original Display Name")
				.query(DataQuery.of("OriginalDisplayName"))
				.build();
		VIEWER_SPAWNED = Key.builder()
				.type(new TypeToken<Value<Boolean>>() {
				})
				.id("viewer_spawned")
				.name("Viewer Spawned")
				.query(DataQuery.of("ViewerSpawned"))
				.build();
		GAME_MODE_EFFECT = Key.builder()
				.type(new TypeToken<Value<GameMode>>() {
				})
				.id("game_mode_effect")
				.name("Game Mode Effect State")
				.query(DataQuery.of("GameModeEffect"))
				.build();

		event.register(ORIGINAL_DISPLAY_NAME);
		event.register(VIEWER_SPAWNED);
		event.register(GAME_MODE_EFFECT);
	}

	@Listener
	public void onDataRegistration(GameRegistryEvent.Register<DataRegistration<?, ?>> event) {
		ORIGINAL_DISPLAY_NAME_DATA_REGISTRATION = DataRegistration.builder()
				.dataClass(OriginalDisplayNameData.class)
				.immutableClass(ImmutableOriginalDisplayNameData.class)
				.dataImplementation(OriginalDisplayNameData.class)
				.immutableImplementation(ImmutableOriginalDisplayNameData.class)
				.builder(new OriginalDisplayNameDataBuilder())
				.id("original_display_name")
				.name("Original Display Name")
				.build();
		VIEWER_SPAWNED_DATA_REGISTRATION = DataRegistration.builder()
				.dataClass(ViewerSpawnedData.class)
				.immutableClass(ImmutableViewerSpawnedData.class)
				.dataImplementation(ViewerSpawnedData.class)
				.immutableImplementation(ImmutableViewerSpawnedData.class)
				.builder(new ViewerSpawnedDataBuilder())
				.id("viewer_spawned")
				.name("Viewer Spawned")
				.build();
		GAME_MODE_EFFECT_DATA_REGISTRATION = DataRegistration.builder()
				.dataClass(GameModeEffectData.class)
				.immutableClass(ImmutableGameModeEffectData.class)
				.dataImplementation(GameModeEffectData.class)
				.immutableImplementation(ImmutableGameModeEffectData.class)
				.builder(new GameModeEffectDataBuilder())
				.id("game_mode_effect")
				.name("Game Mode Effect State")
				.build();
	}

	@Override
	public void initCrowdControl() {
		ConfigurationNode config;
		try {
			config = configLoader.load();
		} catch (IOException e) {
			throw new RuntimeException("Could not load plugin config", e);
		}

		try {
			hosts = Collections.unmodifiableCollection(config.getNode("hosts").getList(TypeToken.of(String.class)));
		} catch (ObjectMappingException e) {
			throw new RuntimeException("Could not parse 'hosts' config variable", e);
		}

		// limit config
		boolean hostsBypass = config.getNode("limits", "hosts-bypass").getBoolean(true);
		TypeToken<Map<String, Integer>> typeToken = new TypeToken<Map<String, Integer>>() {};
		try {
			Map<String, Integer> itemLimits = config.getNode("limits", "items").getValue(typeToken);
			Map<String, Integer> entityLimits = config.getNode("limits", "entities").getValue(typeToken);
			limitConfig = new LimitConfig(hostsBypass, itemLimits, entityLimits);
		} catch (ObjectMappingException e) {
			getSLF4JLogger().warn("Could not parse limits config", e);
		}

		global = config.getNode("global").getBoolean(false);
		announce = config.getNode("announce").getBoolean(true);
		adminRequired = config.getNode("admin-required").getBoolean(false);
		hideNames = HideNames.fromConfigCode(config.getNode("hide-names").getString("none"));
		if (!hosts.isEmpty()) {
			Set<String> loweredHosts = new HashSet<>(hosts.size());
			for (String host : hosts)
				loweredHosts.add(host.toLowerCase(Locale.ROOT));
			hosts = Collections.unmodifiableSet(loweredHosts);
		}
		isServer = !config.getNode("legacy").getBoolean(false);
		int port = config.getNode("port").getInt(DEFAULT_PORT);
		if (isServer) {
			getLogger().info("Running Crowd Control in server mode");
			String password;
			if (manualPassword != null)
				password = manualPassword;
			else {
				password = config.getNode("password").getString("crowdcontrol");
				if (password == null || password.isEmpty()) {
					logger.error("No password has been set in the plugin's config file. Please set one by editing config/crowd-control.conf or set a temporary password using the /password command.");
					return;
				}
			}
			crowdControl = CrowdControl.server().port(port).password(password).build();
		} else {
			getLogger().info("Running Crowd Control in legacy client mode");
			String ip = config.getNode("ip").getString("127.0.0.1");
			if (ip == null || ip.isEmpty()) {
				logger.error("No IP address has been set in the plugin's config file. Please set one by editing config/crowd-control.conf");
				return;
			}
			crowdControl = CrowdControl.client().port(port).ip(ip).build();
		}

		commandRegister.register();
		postInitCrowdControl(crowdControl);
	}

	@SneakyThrows(IOException.class)
	@Listener
	public void onServerStart(GameStartedServerEvent event) {
		CommandConstants.SOUND_VALIDATOR = key -> registry.getType(SoundType.class, key.asString()).isPresent();
		game.getEventManager().registerListeners(this, softLockResolver);
		spongeSerializer = SpongeComponentSerializer.get();
		scheduler = game.getScheduler();
		defaultConfig.copyToFile(configPath, false, true);
		configLoader = HoconConfigurationLoader.builder()
				.setPath(configPath)
				.build();
		initCrowdControl();
		commandManager = new SpongeCommandManager<>(
				pluginContainer,
				AsynchronousCommandExecutionCoordinator.<CommandSource>builder()
						.withAsynchronousParsing().withExecutor(asyncExecutor).build(),
				Function.identity(),
				Function.identity()
		);
		registerChatCommands();
	}

	@Listener
	public void onServerStop(GameStoppingServerEvent event) {
		if (crowdControl != null) {
			crowdControl.shutdown("Minecraft server is shutting down");
			crowdControl = null;
		}
	}

	@Listener
	public void onConnection(ClientConnectionEvent.Join event) {
		onPlayerJoin(event.getTargetEntity());
		Platform platform = game.getPlatform();
		if ((platform.getType().isClient() || platform.getExecutionType().isClient()) && clientHost == null) {
			clientHost = event.getTargetEntity().getUniqueId().toString();
		}
	}

	@Override
	public @NotNull CCPlayer getPlayer(@NotNull Player player) {
		return new SpongePlayer(player);
	}
}
