package dev.qixils.crowdcontrol.plugin.fabric;

import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.fabric.FabricServerCommandManager;
import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.common.EntityMapper;
import dev.qixils.crowdcontrol.common.PlayerEntityMapper;
import dev.qixils.crowdcontrol.common.PlayerManager;
import dev.qixils.crowdcontrol.common.command.Command;
import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.common.mc.CCPlayer;
import dev.qixils.crowdcontrol.common.util.SemVer;
import dev.qixils.crowdcontrol.plugin.configurate.ConfiguratePlugin;
import dev.qixils.crowdcontrol.plugin.fabric.client.FabricPlatformClient;
import dev.qixils.crowdcontrol.plugin.fabric.event.EventManager;
import dev.qixils.crowdcontrol.plugin.fabric.event.Join;
import dev.qixils.crowdcontrol.plugin.fabric.event.Leave;
import dev.qixils.crowdcontrol.plugin.fabric.mc.FabricPlayer;
import dev.qixils.crowdcontrol.plugin.fabric.packets.PacketUtil;
import dev.qixils.crowdcontrol.plugin.fabric.packets.RequestVersionS2C;
import dev.qixils.crowdcontrol.plugin.fabric.packets.ResponseVersionC2S;
import dev.qixils.crowdcontrol.plugin.fabric.utils.MojmapTextUtil;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.kyori.adventure.pointer.Pointered;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.flag.FeatureElement;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static dev.qixils.crowdcontrol.exceptions.ExceptionUtil.validateNotNullElseGet;
import static net.minecraft.resources.ResourceLocation.fromNamespaceAndPath;

/**
 * The main class used by a Crowd Control implementation based on the decompiled code of Minecraft
 * for managing Crowd Control server/client connections and handling
 * {@link Command Commands}.
 */
@Getter
public class FabricCrowdControlPlugin extends ConfiguratePlugin<ServerPlayer, CommandSourceStack> implements ModInitializer {
	// client stuff
	public static boolean CLIENT_INITIALIZED = false;
	public static boolean CLIENT_AVAILABLE = false;
	// packet stuff
	public static ResourceLocation VERSION_REQUEST_ID = fromNamespaceAndPath("crowdcontrol", "version-request");
	public static ResourceLocation VERSION_RESPONSE_ID = fromNamespaceAndPath("crowdcontrol", "version-response");
	public static ResourceLocation SHADER_ID = fromNamespaceAndPath("crowdcontrol", "shader");
	// variables
	@NotNull
	private final EventManager eventManager = new EventManager();
	@Accessors(fluent = true)
	private final CommandRegister commandRegister = new CommandRegister(this);
	@Nullable
	protected MinecraftServer server;
	@Nullable @Accessors(fluent = true)
	protected FabricServerAudiences adventure;
	@NotNull
	private MojmapTextUtil textUtil = new MojmapTextUtil(this);
	@NotNull
	private Executor syncExecutor = runnable -> {
		try {
			if (server != null) {
				server.executeIfPossible(runnable);
			} else {
				runnable.run();
			}
		} catch (Exception e) {
			getSLF4JLogger().error("Error while executing sync task", e);
		}
	};
	private final ExecutorService asyncExecutor = Executors.newCachedThreadPool();
	private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(2);
	private final Logger SLF4JLogger = LoggerFactory.getLogger("CrowdControl");
	private final PlayerManager<ServerPlayer> playerManager = new MojmapPlayerManager(this);
	@Accessors(fluent = true)
	private final PlayerEntityMapper<ServerPlayer> playerMapper = new ServerPlayerMapper(this);
	@Accessors(fluent = true)
	private final EntityMapper<CommandSourceStack> commandSenderMapper = new CommandSourceStackMapper(this);
	private final FabricServerCommandManager<CommandSourceStack> commandManager
			= FabricServerCommandManager.createNative(AsynchronousCommandExecutionCoordinator.<CommandSourceStack>builder()
			.withAsynchronousParsing()
			.withExecutor(getAsyncExecutor())
			.build()
	);
	private final SoftLockResolver softLockResolver = new SoftLockResolver(this);
	private final Map<UUID, SemVer> clientVersions = new HashMap<>();
	private final @NotNull HoconConfigurationLoader configLoader = createConfigLoader(FabricLoader.getInstance().getConfigDir());
	private static @MonotonicNonNull FabricCrowdControlPlugin instance;

	public FabricCrowdControlPlugin() {
		super(ServerPlayer.class, CommandSourceStack.class);
		CommandConstants.SOUND_VALIDATOR = key -> BuiltInRegistries.SOUND_EVENT.containsKey(fromNamespaceAndPath(key.namespace(), key.value()));
		instance = this;
		getEventManager().registerListeners(softLockResolver);
		getEventManager().register(Join.class, join -> onPlayerJoin(join.player()));
		getEventManager().register(Leave.class, leave -> onPlayerLeave(leave.player()));
	}

	@Override
	public void onInitialize() {
		registerChatCommands();
		ServerLifecycleEvents.SERVER_STARTING.register(this::setServer);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> setServer(null));
		PacketUtil.registerPackets();
		ServerPlayNetworking.registerGlobalReceiver(ResponseVersionC2S.PACKET_ID, (payload, context) -> {
			getSLF4JLogger().debug("Received version response from client!");
			clientVersions.put(context.player().getUUID(), payload.version());
			updateConditionalEffectVisibility(crowdControl);
		});
	}

	public <T> Registry<T> registry(ResourceKey<? extends Registry<? extends T>> key, @Nullable RegistryAccess accessor) {
		if (accessor == null) accessor = server().registryAccess();
		return accessor.registryOrThrow(key);
	}

	public <T> Iterable<Holder.Reference<T>> registryHolders(ResourceKey<? extends Registry<? extends T>> key, @Nullable RegistryAccess accessor) {
		Registry<T> registry = registry(key, accessor);
		return new Iterable<>() {
            @NotNull
            @Override
            public Iterator<Holder.Reference<T>> iterator() {
                return registry.holders().iterator();
            }
        };
	}

	@Override
	public @NotNull CCPlayer getPlayer(@NotNull ServerPlayer player) {
		return new FabricPlayer(player);
	}

	/**
	 * Gets the instance of the plugin.
	 *
	 * @return the instance of the plugin
	 */
	@NotNull
	public static FabricCrowdControlPlugin getInstance() {
		if (instance == null)
			throw new IllegalStateException("Plugin instance not initialized");
		return instance;
	}

	/**
	 * Determines whether the plugin instance is available.
	 *
	 * @return {@code true} if the plugin instance is available, {@code false} otherwise
	 */
	public static boolean isInstanceAvailable() {
		return instance != null;
	}

	public boolean isClientAvailable(@Nullable List<ServerPlayer> possiblePlayers, @NotNull Request request) {
		final List<ServerPlayer> players = validateNotNullElseGet(possiblePlayers, () -> getPlayers(request));
		return players.stream().anyMatch(player -> clientVersions.containsKey(player.getUUID()));
	}

	@Override
	public boolean supportsClientOnly() {
		return true;
	}

	@Override
	public @NotNull Collection<String> getHosts() {
		Set<String> hosts = new HashSet<>(super.getHosts());
		if (CLIENT_AVAILABLE) {
			FabricPlatformClient.get()
					.player()
					.map(player -> player.getUUID().toString().toLowerCase(Locale.ENGLISH))
					.ifPresent(hosts::add);
		}
		return hosts;
	}

	@NotNull
	public MinecraftServer server() throws IllegalStateException {
		if (this.server == null)
			throw new IllegalStateException("Tried to access server without one running");
		return this.server;
	}

	@NotNull
	public FabricServerAudiences adventure() throws IllegalStateException {
		if (this.adventure == null)
			throw new IllegalStateException("Tried to access Adventure without running a server");
		return this.adventure;
	}

	@NotNull
	public Optional<FabricServerAudiences> adventureOptional() {
		return Optional.ofNullable(this.adventure);
	}

	protected void setServer(@Nullable MinecraftServer server) {
		if (server == null) {
			this.server = null;
			this.adventure = null;
			this.syncExecutor = Runnable::run;
			if (this.crowdControl != null) {
				this.crowdControl.shutdown("Server is shutting down");
				this.crowdControl = null;
			}
		} else {
			this.server = server;
			this.adventure = FabricServerAudiences.of(server);
			this.syncExecutor = server;
			this.textUtil = new MojmapTextUtil(this);
			initCrowdControl();
		}
	}

	@Override
	public @NotNull Audience getConsole() {
		return adventure().console();
	}

	@Override
	public boolean canGetModVersion() {
		return true;
	}

	@Override
	public @NotNull Optional<SemVer> getModVersion(@NotNull ServerPlayer player) {
		return Optional.ofNullable(clientVersions.get(player.getUUID()));
	}

	@Override
	public int getModdedPlayerCount() {
		return clientVersions.size();
	}

	@Override
	public void onPlayerJoin(ServerPlayer player) {
		// put client version
		if (CLIENT_AVAILABLE) {
			FabricPlatformClient.get().player()
					.map(LocalPlayer::getUUID)
					.filter(uuid -> uuid.equals(player.getUUID()))
					.ifPresent(uuid -> clientVersions.put(uuid, SemVer.MOD));
		}
		// request client version if not available
		if (!clientVersions.containsKey(player.getUUID())) {
			getSLF4JLogger().debug("Sending version request to " + player.getUUID());
			ServerPlayNetworking.send(player, RequestVersionS2C.INSTANCE);
		}
		// super
		super.onPlayerJoin(player);
	}

	@Override
	public void onPlayerLeave(ServerPlayer player) {
		clientVersions.remove(player.getUUID());
		super.onPlayerLeave(player);
	}

	@Override
	public @Nullable ServerPlayer asPlayer(@NotNull CommandSourceStack sender) {
		return sender.getPlayer();
	}

	@NotNull
	public TriState isEnabled(FeatureElement feature) {
		if (server == null) return TriState.UNKNOWN;
		return TriState.fromBoolean(feature.isEnabled(server.getWorldData().enabledFeatures()));
	}

	public boolean isDisabled(FeatureElement feature) {
		return isEnabled(feature) == TriState.FALSE;
	}

	public @NotNull Component toAdventure(ComponentLike text, @NotNull Pointered viewer) {
		return adventure().renderer().render(text.asComponent(), viewer);
	}

	public @NotNull net.minecraft.network.chat.Component toNative(ComponentLike text, @NotNull Pointered viewer) {
		return adventure().toNative(toAdventure(text, viewer));
	}

	@Override
	public InputStream getInputStream(@NotNull String asset) {
		Optional<Path> path = FabricLoader.getInstance()
			.getModContainer("crowdcontrol")
			.flatMap(container -> container.findPath(asset));

        if (path.isEmpty())
            return null;

        try {
			return Files.newInputStream(path.get());
        } catch (IOException e) {
            SLF4JLogger.warn(String.format("Encountered exception while retrieving asset %s", asset), e);
			return null;
        }
	}
}
