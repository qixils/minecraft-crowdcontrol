package dev.qixils.crowdcontrol.plugin.mojmap;

import dev.qixils.crowdcontrol.common.CommandConstants;
import dev.qixils.crowdcontrol.common.EntityMapper;
import dev.qixils.crowdcontrol.common.PlayerManager;
import dev.qixils.crowdcontrol.common.util.TextUtil;
import dev.qixils.crowdcontrol.plugin.configurate.AbstractPlugin;
import dev.qixils.crowdcontrol.plugin.mojmap.utils.WrappedAudienceProvider;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.platform.AudienceProvider;
import net.minecraft.Util;
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

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// TODO: implement soft lock resolver

/**
 * The main class used by a Crowd Control implementation based on the decompiled code of Minecraft
 * for managing Crowd Control server/client connections and handling
 * {@link dev.qixils.crowdcontrol.common.Command Commands}.
 */
@Getter
public abstract class MojmapPlugin extends AbstractPlugin<ServerPlayer, CommandSourceStack> {
	private static final TextUtil EMPTY_TEXT_UTIL = new TextUtil(null);
	@Accessors(fluent = true)
	private final CommandRegister commandRegister = new CommandRegister(this);
	@Nullable
	protected MinecraftServer server;
	@Nullable @Accessors(fluent = true)
	protected WrappedAudienceProvider adventure;
	@NotNull
	private TextUtil textUtil = EMPTY_TEXT_UTIL;
	// TODO is this actually the sync executor?? 'Main' sounds sync but 'background' doesn't
	private final ExecutorService syncExecutor = Util.backgroundExecutor();
	private final ExecutorService asyncExecutor = Executors.newCachedThreadPool();
	private final Logger SLF4JLogger = LoggerFactory.getLogger("crowd-control");
	private final PlayerManager<ServerPlayer> playerManager = new MojmapPlayerManager(this);
	@Accessors(fluent = true)
	private final EntityMapper<ServerPlayer> playerMapper = new PlayerEntityMapper(this);
	private @MonotonicNonNull HoconConfigurationLoader configLoader;

	protected MojmapPlugin() {
		super(ServerPlayer.class, CommandSourceStack.class);
		CommandConstants.SOUND_VALIDATOR = key -> Registry.SOUND_EVENT.containsKey(new ResourceLocation(key.namespace(), key.value()));
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
	public WrappedAudienceProvider adventure() throws IllegalStateException {
		if (this.adventure == null)
			throw new IllegalStateException("Tried to access Adventure without running a server");
		return this.adventure;
	}

	@NotNull
	protected abstract AudienceProvider initAdventure(@NotNull MinecraftServer server);

	protected void setServer(@Nullable MinecraftServer server) {
		if (server == null) {
			this.server = null;
			this.adventure = null;
			this.textUtil = EMPTY_TEXT_UTIL;
		} else {
			this.server = server;
			this.adventure = new WrappedAudienceProvider(initAdventure(server));
			this.textUtil = new TextUtil(this.adventure.flattener());
			this.configLoader = createConfigLoader(server.getFile("config"));
		}
	}
}
