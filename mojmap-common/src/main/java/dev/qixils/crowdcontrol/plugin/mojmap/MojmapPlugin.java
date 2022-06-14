package dev.qixils.crowdcontrol.plugin.mojmap;

import dev.qixils.crowdcontrol.common.CommandConstants;
import dev.qixils.crowdcontrol.common.EntityMapper;
import dev.qixils.crowdcontrol.common.PlayerManager;
import dev.qixils.crowdcontrol.plugin.configurate.AbstractPlugin;
import dev.qixils.crowdcontrol.plugin.mojmap.event.EventManager;
import dev.qixils.crowdcontrol.plugin.mojmap.utils.MojmapTextUtil;
import dev.qixils.crowdcontrol.plugin.mojmap.utils.WrappedAudienceProvider;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.platform.AudienceProvider;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

// TODO: implement soft lock resolver

/**
 * The main class used by a Crowd Control implementation based on the decompiled code of Minecraft
 * for managing Crowd Control server/client connections and handling
 * {@link dev.qixils.crowdcontrol.common.Command Commands}.
 */
@Getter
public abstract class MojmapPlugin<P extends AudienceProvider> extends AbstractPlugin<ServerPlayer, CommandSourceStack> {
	// accessors
	public static final EntityDataAccessor<Optional<Component>> ORIGINAL_DISPLAY_NAME = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.OPTIONAL_COMPONENT);
	public static final EntityDataAccessor<Boolean> VIEWER_SPAWNED = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.BOOLEAN);
	public static final EntityDataAccessor<String> GAME_MODE_EFFECT = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.STRING);
	// variables
	@NotNull
	private final EventManager eventManager = new EventManager();
	@Accessors(fluent = true)
	private final CommandRegister commandRegister = new CommandRegister(this);
	@Nullable
	protected MinecraftServer server;
	@Nullable @Accessors(fluent = true)
	protected WrappedAudienceProvider<P> adventure;
	@NotNull
	private MojmapTextUtil textUtil = new MojmapTextUtil(this);
	// TODO is this actually the sync executor?? 'Main' sounds sync but 'background' doesn't
	private final ExecutorService syncExecutor = Util.backgroundExecutor();
	private final ExecutorService asyncExecutor = Executors.newCachedThreadPool();
	private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(2);
	private final Logger SLF4JLogger = LoggerFactory.getLogger("crowd-control");
	private final PlayerManager<ServerPlayer> playerManager = new MojmapPlayerManager(this);
	@Accessors(fluent = true)
	private final EntityMapper<ServerPlayer> playerMapper = new PlayerEntityMapper(this);
	private @MonotonicNonNull HoconConfigurationLoader configLoader;
	private static @MonotonicNonNull MojmapPlugin<?> instance;

	protected MojmapPlugin() {
		super(ServerPlayer.class, CommandSourceStack.class);
		CommandConstants.SOUND_VALIDATOR = key -> Registry.SOUND_EVENT.containsKey(new ResourceLocation(key.namespace(), key.value()));
		instance = this;
	}

	/**
	 * Gets the instance of the plugin.
	 *
	 * @return the instance of the plugin
	 */
	@NotNull
	public static MojmapPlugin<?> getInstance() {
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

	public abstract boolean isClientAvailable(@Nullable List<ServerPlayer> possiblePlayers, @NotNull Request request);

	@Override
	public boolean supportsClientOnly() {
		return true;
	}

	@NotNull
	public MinecraftServer server() throws IllegalStateException {
		if (this.server == null)
			throw new IllegalStateException("Tried to access server without one running");
		return this.server;
	}

	@NotNull
	public WrappedAudienceProvider<P> adventure() throws IllegalStateException {
		if (this.adventure == null)
			throw new IllegalStateException("Tried to access Adventure without running a server");
		return this.adventure;
	}

	@NotNull
	public Optional<WrappedAudienceProvider<P>> adventureOptional() {
		return Optional.ofNullable(this.adventure);
	}

	@NotNull
	protected abstract WrappedAudienceProvider<P> initAdventure(@NotNull MinecraftServer server);

	protected void setServer(@Nullable MinecraftServer server) {
		if (server == null) {
			this.server = null;
			this.adventure = null;
		} else {
			this.server = server;
			this.adventure = initAdventure(server);
			this.textUtil = new MojmapTextUtil(this);
			this.configLoader = createConfigLoader(server.getFile("config"));
		}
	}
}
