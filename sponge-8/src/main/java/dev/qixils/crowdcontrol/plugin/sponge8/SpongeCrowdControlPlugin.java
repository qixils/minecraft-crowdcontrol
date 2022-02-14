package dev.qixils.crowdcontrol.plugin.sponge8;

import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.sponge.SpongeCommandManager;
import com.google.inject.Inject;
import dev.qixils.crowdcontrol.CrowdControl;
import dev.qixils.crowdcontrol.common.AbstractPlugin;
import dev.qixils.crowdcontrol.common.CommandConstants;
import dev.qixils.crowdcontrol.common.EntityMapper;
import dev.qixils.crowdcontrol.common.util.TextUtil;
import dev.qixils.crowdcontrol.exceptions.ExceptionUtil;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Game;
import org.spongepowered.api.Platform;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataStore;
import org.spongepowered.api.data.type.MatterType;
import org.spongepowered.api.data.type.MatterTypes;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.LoadedGameEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.RegisterDataEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.TaskExecutorService;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;

@Getter
@Plugin("crowd-control")
@ParametersAreNonnullByDefault
public class SpongeCrowdControlPlugin extends AbstractPlugin<ServerPlayer, CommandCause> {
	// keys
	public static Key<Value<Component>> ORIGINAL_DISPLAY_NAME; // TODO: component cannot be serialized ??
	public static Key<Value<Boolean>> VIEWER_SPAWNED;
	public static Key<Value<GameMode>> GAME_MODE_EFFECT;
	// "real" variables
	private final SoftLockResolver softLockResolver = new SoftLockResolver(this);
	private final Logger logger = LoggerFactory.getLogger("crowd-control");
	private final CommandRegister register = new CommandRegister(this);
	private final TextUtil textUtil = new TextUtil(null);
	private final SpongePlayerManager playerManager = new SpongePlayerManager(this);
	@Accessors(fluent = true)
	private final EntityMapper<CommandCause> commandSenderMapper = new CommandCauseMapper();
	@Accessors(fluent = true)
	private final EntityMapper<ServerPlayer> playerMapper = new ServerPlayerMapper();
	private SpongeCommandManager<CommandCause> commandManager;
	private ConfigurationLoader<CommentedConfigurationNode> configLoader;
	private String clientHost = null;
	private Scheduler syncScheduler;
	private Scheduler asyncScheduler;
	private TaskExecutorService syncExecutor;
	private TaskExecutorService asyncExecutor;
	// injected variables
	@Inject
	private PluginContainer pluginContainer;
	@Inject
	private Game game;
	@Inject
	@DefaultConfig(sharedRoot = true)
	private Path configPath;

	public SpongeCrowdControlPlugin() {
		super(ServerPlayer.class, CommandCause.class);
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

	public static boolean isMatter(BlockState block, MatterType matter) {
		return block.get(Keys.MATTER_TYPE).map(actual -> actual.equals(matter)).orElse(false);
	}

	public static boolean isLiquid(BlockState block) {
		return isMatter(block, MatterTypes.LIQUID.get());
	}

	public <T> Iterable<T> registryIterable(RegistryType<T> registryType) {
		return () -> game.registry(registryType).stream().iterator();
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
			return configLoader.load().node("password").getString();
		} catch (IOException e) {
			logger.warn("Could not load config", e);
			return null;
		}
	}

	@Listener
	public void onKeyRegistration(RegisterDataEvent event) {
		ORIGINAL_DISPLAY_NAME = Key.builder()
				.elementType(Component.class)
				.key(ResourceKey.of(pluginContainer, "original_display_name"))
				// TODO may need a comparator
				.build();
		VIEWER_SPAWNED = Key.builder()
				.elementType(Boolean.class)
				.key(ResourceKey.of(pluginContainer, "viewer_spawned"))
				.build();
		GAME_MODE_EFFECT = Key.builder()
				.elementType(GameMode.class)
				.key(ResourceKey.of(pluginContainer, "game_mode_effect"))
				.build();

		DataRegistration ogNameRegister = DataRegistration.builder()
				.dataKey(ORIGINAL_DISPLAY_NAME)
				.store(DataStore.of(
						ORIGINAL_DISPLAY_NAME,
						DataQuery.of("GameModeEffect"),
						Entity.class
				))
				.build();
		DataRegistration viewerRegister = DataRegistration.builder()
				.dataKey(VIEWER_SPAWNED)
				.store(DataStore.of(
						VIEWER_SPAWNED,
						DataQuery.of("ViewerSpawned"),
						Entity.class
				))
				.build();
		DataRegistration gameModeRegister = DataRegistration.builder()
				.dataKey(GAME_MODE_EFFECT)
				.store(DataStore.of(
						GAME_MODE_EFFECT,
						DataQuery.of("GameModeEffect"),
						ServerPlayer.class
				))
				.build();

		event.register(ogNameRegister);
		event.register(viewerRegister);
		event.register(gameModeRegister);
	}

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
				password = config.node("password").getString("crowdcontrol");
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

	@Listener
	public void onCommandRegister(RegisterCommandEvent<Command.Raw> event) {
		// TODO this is not working; move this to guice injection
		commandManager = new SpongeCommandManager<>(
				pluginContainer,
				AsynchronousCommandExecutionCoordinator.<CommandCause>newBuilder()
						.withAsynchronousParsing().withExecutor(asyncExecutor).build(),
				Function.identity(),
				Function.identity()
		);
		registerChatCommands();
	}

	@Listener
	public void onServerStart(StartingEngineEvent<Server> event) {
		syncScheduler = game.server().scheduler();
		asyncScheduler = game.asyncScheduler();
		syncExecutor = syncScheduler.executor(pluginContainer);
		asyncExecutor = asyncScheduler.executor(pluginContainer);
		if (!configPath.toFile().exists()) {
			// read the default config
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream("default.conf");
			if (inputStream == null)
				throw new IllegalStateException("Could not find default config file; please report to qixils");
			// copy the default config to the config path
			try {
				Files.copy(inputStream, configPath);
			} catch (IOException e) {
				throw new IllegalStateException("Could not copy default config file to " + configPath, e);
			}
		}
		configLoader = HoconConfigurationLoader.builder()
				.path(configPath)
				.build();
		initCrowdControl();
	}

	@Listener
	public void onServerStop(StoppingEngineEvent<Server> event) {
		syncScheduler = null;
		syncExecutor = null;
		if (crowdControl != null) {
			crowdControl.shutdown("Minecraft server is shutting down");
			crowdControl = null;
		}
	}

	@Listener
	public void onLoad(LoadedGameEvent event) {
		game.eventManager().registerListeners(pluginContainer, softLockResolver);
	}

	@Listener
	public void onConnection(ServerSideConnectionEvent.Join event) {
		onPlayerJoin(event.player());
		Platform platform = game.platform();
		if ((platform.type().isClient() || platform.executionType().isClient() || game.isClientAvailable())
				&& clientHost == null) {
			clientHost = event.player().uniqueId().toString();
		}
	}
}
