package dev.qixils.crowdcontrol.plugin.sponge8;

import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.sponge.SpongeCommandManager;
import dev.qixils.crowdcontrol.exceptions.ExceptionUtil;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.math.vector.Vector3d;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import dev.qixils.crowdcontrol.CrowdControl;
import dev.qixils.crowdcontrol.common.AbstractPlugin;
import dev.qixils.crowdcontrol.common.CommandConstants;
import dev.qixils.crowdcontrol.plugin.sponge8.data.entity.GameModeEffectData;
import dev.qixils.crowdcontrol.plugin.sponge8.data.entity.GameModeEffectDataBuilder;
import dev.qixils.crowdcontrol.plugin.sponge8.data.entity.ImmutableGameModeEffectData;
import dev.qixils.crowdcontrol.plugin.sponge8.data.entity.ImmutableOriginalDisplayNameData;
import dev.qixils.crowdcontrol.plugin.sponge8.data.entity.ImmutableViewerSpawnedData;
import dev.qixils.crowdcontrol.plugin.sponge8.data.entity.OriginalDisplayNameData;
import dev.qixils.crowdcontrol.plugin.sponge8.data.entity.OriginalDisplayNameDataBuilder;
import dev.qixils.crowdcontrol.plugin.sponge8.data.entity.ViewerSpawnedData;
import dev.qixils.crowdcontrol.plugin.sponge8.data.entity.ViewerSpawnedDataBuilder;
import dev.qixils.crowdcontrol.plugin.sponge8.utils.Sponge7TextUtil;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static net.kyori.adventure.key.Key.MINECRAFT_NAMESPACE;

// TODO plugin file
@Getter
public class SpongeCrowdControlPlugin extends AbstractPlugin<ServerPlayer, Subject> { // TODO idk abt these interfaces
	// keys (though they don't really work)
	public static Key<Value<Component>> ORIGINAL_DISPLAY_NAME = DummyObjectProvider.createExtendedFor(Key.class, "ORIGINAL_DISPLAY_NAME");
	public static Key<Value<Boolean>> VIEWER_SPAWNED = DummyObjectProvider.createExtendedFor(Key.class, "VIEWER_SPAWNED");
	public static Key<Value<GameMode>> GAME_MODE_EFFECT = DummyObjectProvider.createExtendedFor(Key.class, "GAME_MODE_EFFECT");
	// "real" variables
	private final CommandRegister register = new CommandRegister(this);
	private final Sponge7TextUtil textUtil = new Sponge7TextUtil();
	private final SpongePlayerMapper playerMapper = new SpongePlayerMapper(this);
	private SpongeCommandManager<Subject> commandManager;
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
	// registries
	private DataRegistration<OriginalDisplayNameData, ImmutableOriginalDisplayNameData> ORIGINAL_DISPLAY_NAME_DATA_REGISTRATION;
	private DataRegistration<ViewerSpawnedData, ImmutableViewerSpawnedData> VIEWER_SPAWNED_DATA_REGISTRATION;
	private DataRegistration<GameModeEffectData, ImmutableGameModeEffectData> GAME_MODE_EFFECT_DATA_REGISTRATION;

	public SpongeCrowdControlPlugin() {
		super(ServerPlayer.class, Subject.class);
	}

	public static void spawnPlayerParticles(Entity entity, ParticleEffect particle) {
		World<?, ?> world = entity.world();
		Location<?, ?> location = entity.location();
		Vector3d position = new Vector3d(location.x(), Math.ceil(location.y()), location.z());
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

	@Override
	public boolean isAdmin(@NotNull Subject commandSource) {
		return commandSource.hasPermission(ADMIN_PERMISSION);
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

	@SuppressWarnings("UnstableApiUsage")
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

	@SuppressWarnings("UnstableApiUsage")
	@Override
	public void initCrowdControl() {
		CommandConstants.SOUND_VALIDATOR = key -> game.registry(RegistryTypes.SOUND_TYPE).findEntry(ResourceKey.resolve(key.asString())).isPresent();

		ConfigurationNode config;
		try {
			config = configLoader.load();
		} catch (IOException e) {
			throw new RuntimeException("Could not load plugin config", e);
		}

		try {
			hosts = Collections.unmodifiableCollection(ExceptionUtil.validateNotNullElseGet(
					config.node("hosts").getList(String.class),
					Collections::emptyList
			));
		} catch (SerializationException e) {
			throw new RuntimeException("Could not parse 'hosts' config variable", e);
		}

		global = config.node("global").getBoolean(false);
		announce = config.node("announce").getBoolean(true);
		if (!hosts.isEmpty()) {
			Set<String> loweredHosts = new HashSet<>(hosts.size());
			for (String host : hosts)
				loweredHosts.add(host.toLowerCase(Locale.ROOT));
			hosts = Collections.unmodifiableSet(loweredHosts);
		}
		isServer = !config.node("legacy").getBoolean(false);
		int port = config.node("port").getInt(DEFAULT_PORT);
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
			String ip = config.node("ip").getString("127.0.0.1");
			if (ip == null || ip.isEmpty()) {
				logger.error("No IP address has been set in the plugin's config file. Please set one by editing config/crowd-control.conf");
				return;
			}
			crowdControl = CrowdControl.client().port(port).ip(ip).build();
		}

		register.register();
	}

	@SneakyThrows(IOException.class)
	@Listener
	public void onServerStart(GameStartedServerEvent event) {
		spongeSerializer = SpongeComponentSerializer.get();
		scheduler = game.getScheduler();
		defaultConfig.copyToFile(configPath, false, true);
		configLoader = HoconConfigurationLoader.builder()
				.setPath(configPath)
				.build();
		initCrowdControl();
		commandManager = new SpongeCommandManager<>(
				pluginContainer,
				AsynchronousCommandExecutionCoordinator.<CommandSource>newBuilder()
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
}
