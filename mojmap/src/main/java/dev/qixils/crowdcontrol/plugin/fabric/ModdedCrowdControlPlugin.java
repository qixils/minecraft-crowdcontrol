package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.common.EntityMapper;
import dev.qixils.crowdcontrol.common.PlayerEntityMapper;
import dev.qixils.crowdcontrol.common.PlayerManager;
import dev.qixils.crowdcontrol.common.Plugin;
import dev.qixils.crowdcontrol.common.command.Command;
import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.common.mc.MCCCPlayer;
import dev.qixils.crowdcontrol.common.packets.ExtraFeaturePacketC2S;
import dev.qixils.crowdcontrol.common.packets.PluginPacket;
import dev.qixils.crowdcontrol.common.packets.VersionRequestPacketS2C;
import dev.qixils.crowdcontrol.common.packets.VersionResponsePacketC2S;
import dev.qixils.crowdcontrol.common.packets.util.ExtraFeature;
import dev.qixils.crowdcontrol.common.util.SemVer;
import dev.qixils.crowdcontrol.plugin.fabric.event.EventManager;
import dev.qixils.crowdcontrol.plugin.fabric.event.Join;
import dev.qixils.crowdcontrol.plugin.fabric.event.Leave;
import dev.qixils.crowdcontrol.plugin.fabric.mc.FabricPlayer;
import dev.qixils.crowdcontrol.plugin.fabric.packets.ServerPacketContext;
import dev.qixils.crowdcontrol.plugin.fabric.utils.ClientAdapter;
import dev.qixils.crowdcontrol.plugin.fabric.utils.MojmapTextUtil;
import dev.qixils.crowdcontrol.plugin.fabric.utils.PermissionUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.kyori.adventure.pointer.Pointered;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Stream;

// TODO: can no longer connect to paper servers

/**
 * The main class used by a Crowd Control implementation based on the decompiled code of Minecraft
 * for managing Crowd Control server/client connections and handling
 * {@link Command Commands}.
 */
@Getter
public abstract class ModdedCrowdControlPlugin extends Plugin<ServerPlayer, CommandSourceStack> {
	// client stuff
	public static boolean CLIENT_INITIALIZED = false;
	public static boolean CLIENT_AVAILABLE = false;
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
				server.execute(runnable);
			} else {
				runnable.run();
			}
		} catch (Exception e) {
			getSLF4JLogger().error("Error while executing sync task", e);
		}
	};
	private final ExecutorService asyncExecutor = Executors.newCachedThreadPool();
	private final Logger SLF4JLogger = LoggerFactory.getLogger("CrowdControl");
	private final PlayerManager<ServerPlayer> playerManager = new MojmapPlayerManager(this);
	@Accessors(fluent = true)
	private final PlayerEntityMapper<ServerPlayer> playerMapper = new ServerPlayerMapper(this);
	@Accessors(fluent = true)
	private final EntityMapper<CommandSourceStack> commandSenderMapper = new CommandSourceStackMapper(this);
	private final SoftLockResolver softLockResolver = new SoftLockResolver(this);
	private final @NotNull HoconConfigurationLoader configLoader = createConfigLoader(Path.of("config"), "crowdcontrol.conf", HoconConfigurationLoader.builder());
	private static @MonotonicNonNull ModdedCrowdControlPlugin instance;
	@Getter @Setter
	private boolean paused = false;

	public ModdedCrowdControlPlugin() {
		super(ServerPlayer.class, CommandSourceStack.class);
		CommandConstants.SOUND_VALIDATOR = key -> Registry.SOUND_EVENT.containsKey(ResourceLocation.tryParse(key.namespace() + ':' + key.value()));
		instance = this;
		getEventManager().registerListeners(softLockResolver);
		getEventManager().register(Join.class, join -> onPlayerJoin(join.player()));
		getEventManager().register(Leave.class, leave -> onPlayerLeave(leave.player()));
	}

	public void onInitialize() {
		getSLF4JLogger().debug("Initializing");
		registerChatCommands();
		MinecraftEvents.SERVER_STARTING.register(newServer -> {
			getSLF4JLogger().debug("Server starting");
			setServer(newServer);
		});
		MinecraftEvents.SERVER_STOPPING.register($ -> shutdown());
		MinecraftEvents.SERVER_STOPPED.register(newServer -> {
			getSLF4JLogger().debug("Server stopping");
			setServer(null);
		});
	}

	@Override
	public @NotNull MCCCPlayer getPlayer(@NotNull ServerPlayer player) {
		return new FabricPlayer(player);
	}

	/**
	 * Gets the instance of the plugin.
	 *
	 * @return the instance of the plugin
	 */
	@NotNull
	public static ModdedCrowdControlPlugin getInstance() {
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

	@Override
	public @NotNull Collection<String> getHosts() {
		Set<String> hosts = new HashSet<>(super.getHosts());
		ClientAdapter.getLocalPlayerId().map(UUID::toString).ifPresent(hosts::add);
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
		} else {
			this.server = server;
			this.adventure = FabricServerAudiences.of(server);
			this.syncExecutor = server;
			this.textUtil = new MojmapTextUtil(this);
			initCrowdControl();
		}
	}

	@Override
	public CompletableFuture<?> shutdown() {
		var superShutdown = super.shutdown();
		asyncExecutor.shutdown();
		return CompletableFuture.allOf(
			superShutdown,
			CompletableFuture.supplyAsync(() -> {
				try {
					return asyncExecutor.awaitTermination(3, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			})
		);
	}

	@Override
	public @NotNull Audience getConsole() {
		return adventure().console();
	}

	@Override
	public void onPlayerJoin(ServerPlayer player) {
		// put client version
		ClientAdapter.getLocalPlayerId().ifPresent(uuid -> clientVersions.put(uuid, SemVer.MOD));
		// request client version if not available
		if (!clientVersions.containsKey(player.getUUID())) {
			getSLF4JLogger().debug("Sending version request to {}", player.getUUID());
			sendToPlayer(player, VersionRequestPacketS2C.INSTANCE);
		}
		// super
		super.onPlayerJoin(player);
	}

	@Override
	public @Nullable ServerPlayer asPlayer(@NotNull CommandSourceStack sender) {
		try {
			return sender.getPlayerOrException();
		} catch (Exception e) {
			return null;
		}
	}

	public @NotNull Component toAdventure(net.minecraft.network.chat.Component vanilla) {
		return adventure().toAdventure(vanilla);
	}

	public @NotNull Component toAdventure(Component text, @NotNull Pointered viewer) {
		return adventure().renderer().render(text, viewer);
	}

	public @NotNull Component toAdventure(net.minecraft.network.chat.Component vanilla, @NotNull Pointered viewer) {
		return toAdventure(toAdventure(vanilla), viewer);
	}

	public @NotNull net.minecraft.network.chat.Component toNative(Component text, @NotNull Pointered viewer) {
		return adventure().toNative(toAdventure(text, viewer));
	}

	// idk how they missed this in old adventure lol
	public static @NotNull Keyed toKeyed(Key loc) {
		return () -> loc;
	}

	public abstract PermissionUtil getPermissionUtil();

	public @NotNull Stream<ServerPlayer> toPlayerStream(@Nullable Collection<UUID> uuids) {
		MinecraftServer _server = server;
		if (uuids == null || _server == null) return Stream.empty();
		return uuids.stream().map(id -> _server.getPlayerList().getPlayer(id)).filter(Objects::nonNull);
	}

	public @NotNull List<ServerPlayer> toPlayerList(@Nullable Collection<UUID> uuids) {
		return toPlayerStream(uuids).toList();
	}

	public void handleVersionResponse(VersionResponsePacketC2S payload, ServerPacketContext context) {
		UUID uuid = context.player().getUUID();
		SemVer version = payload.version();
		getSLF4JLogger().info("Received version {} from client {}", version, uuid);
		clientVersions.put(uuid, version);
		updateConditionalEffectVisibility(uuid);
	}

	public void handleExtraFeatures(ExtraFeaturePacketC2S payload, ServerPacketContext context) {
		UUID uuid = context.player().getUUID();
		Set<ExtraFeature> features = payload.features();
		getSLF4JLogger().info("Received features {} from client {}", features, uuid);
		extraFeatures.put(uuid, features);
		updateConditionalEffectVisibility(uuid);
	}

	public abstract void sendToPlayer(@NotNull ServerPlayer player, @NotNull PluginPacket payload);

	public static void sendToPlayerStatic(@NotNull ServerPlayer player, @NotNull PluginPacket payload) {
		if (!isInstanceAvailable()) return;
		getInstance().sendToPlayer(player, payload);
	}
}
