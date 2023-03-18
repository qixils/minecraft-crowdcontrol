package dev.qixils.crowdcontrol.plugin.sponge8;

import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.sponge.CloudInjectionModule;
import cloud.commandframework.sponge.SpongeCommandManager;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import dev.qixils.crowdcontrol.common.EntityMapper;
import dev.qixils.crowdcontrol.common.PlayerEntityMapper;
import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.common.mc.CCPlayer;
import dev.qixils.crowdcontrol.plugin.configurate.ConfiguratePlugin;
import dev.qixils.crowdcontrol.plugin.sponge8.mc.SpongePlayer;
import dev.qixils.crowdcontrol.plugin.sponge8.utils.SpongeTextUtil;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Game;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
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
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
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
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.file.Path;
import java.util.*;

@Getter
@Plugin("crowdcontrol")
@ParametersAreNonnullByDefault
public class SpongeCrowdControlPlugin extends ConfiguratePlugin<ServerPlayer, CommandCause> {
	// keys
	public static Key<Value<String>> ORIGINAL_DISPLAY_NAME; // TODO: report inability to serialize components
	public static Key<Value<Boolean>> VIEWER_SPAWNED;
	public static Key<Value<ResourceKey>> GAME_MODE_EFFECT;
	// "real" variables
	private final SoftLockResolver softLockResolver = new SoftLockResolver(this);
	private final Logger logger = LoggerFactory.getLogger("CrowdControl");
	@Accessors(fluent = true)
	private final CommandRegister commandRegister = new CommandRegister(this);
	private final SpongeTextUtil textUtil = new SpongeTextUtil();
	private final SpongePlayerManager playerManager = new SpongePlayerManager(this);
	@Accessors(fluent = true)
	private final EntityMapper<CommandCause> commandSenderMapper = new CommandCauseMapper(this);
	@Accessors(fluent = true)
	private final PlayerEntityMapper<ServerPlayer> playerMapper = new ServerPlayerMapper(this);
	private final GsonComponentSerializer serializer = GsonComponentSerializer.gson();
	private final SpongeCommandManager<CommandCause> commandManager;
	private final HoconConfigurationLoader configLoader;
	private Scheduler syncScheduler;
	private Scheduler asyncScheduler;
	private TaskExecutorService syncExecutor;
	private TaskExecutorService asyncExecutor;
	// injected variables
	private final PluginContainer pluginContainer;
	private final Game game;

	@Inject
	public SpongeCrowdControlPlugin(final @NotNull Injector injector, final @DefaultConfig(sharedRoot = true) Path configPath) {
		super(ServerPlayer.class, CommandCause.class);

		this.game = injector.getInstance(Game.class);
		this.pluginContainer = injector.getInstance(PluginContainer.class);
		this.configLoader = createConfigLoader(configPath);

		// create child injector with cloud module
		final Injector childInjector = injector.createChildInjector(
				CloudInjectionModule.createNative(CommandExecutionCoordinator.simpleCoordinator())
		);
		// create command manager instance
		this.commandManager = childInjector.getInstance(com.google.inject.Key.get(new TypeLiteral<SpongeCommandManager<CommandCause>>() {
		}));
		// register chat commands
		registerChatCommands();
		// register event listeners
		game.eventManager().registerListeners(pluginContainer, softLockResolver);
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

	public <T> Iterator<T> registryIterator(RegistryType<T> registryType) {
		return game.registry(registryType).stream().iterator();
	}

	public <T> Iterable<T> registryIterable(RegistryType<T> registryType) {
		return () -> registryIterator(registryType);
	}

	@Override
	public @NotNull Logger getSLF4JLogger() {
		return logger;
	}

	@Override
	public boolean supportsClientOnly() {
		return true;
	}

	@Override
	public @NotNull Collection<String> getHosts() {
		Collection<String> confHosts = super.getHosts();
		if (!Sponge.isClientAvailable()) {
			getSLF4JLogger().debug("No client available, using only config hosts");
			return confHosts;
		}
		getSLF4JLogger().debug("Adding client host to config hosts");
		Set<String> hosts = new HashSet<>(confHosts.size() + 1);
		hosts.addAll(confHosts);
		Sponge.client().player().ifPresent(player -> {
			String host = player.uniqueId().toString().toLowerCase(Locale.ENGLISH);
			getSLF4JLogger().debug("Adding client host {}", host);
			hosts.add(host);
		});
		return hosts;
	}

	@Override
	public @NotNull Audience getConsole() {
		return game.systemSubject();
	}

	@Listener
	public void onKeyRegistration(RegisterDataEvent event) {
		ORIGINAL_DISPLAY_NAME = Key.builder()
				.elementType(String.class)
				.key(ResourceKey.of(pluginContainer, "original_display_name"))
				.build();
		VIEWER_SPAWNED = Key.builder()
				.elementType(Boolean.class)
				.key(ResourceKey.of(pluginContainer, "viewer_spawned"))
				.build();
		GAME_MODE_EFFECT = Key.builder()
				.elementType(ResourceKey.class)
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

	@Listener
	public void onServerStart(StartingEngineEvent<Server> event) {
		CommandConstants.SOUND_VALIDATOR = key -> game.registry(RegistryTypes.SOUND_TYPE).findEntry(ResourceKey.resolve(key.asString())).isPresent();
		syncScheduler = game.server().scheduler();
		asyncScheduler = game.asyncScheduler();
		syncExecutor = syncScheduler.executor(pluginContainer);
		asyncExecutor = asyncScheduler.executor(pluginContainer);
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
	public void onConnection(ServerSideConnectionEvent.Join event) {
		onPlayerJoin(event.player());
	}

	@Override
	public @NotNull CCPlayer getPlayer(@NotNull ServerPlayer player) {
		return new SpongePlayer(player);
	}
}
