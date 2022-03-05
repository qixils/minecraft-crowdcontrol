package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.common.AbstractPlugin;
import dev.qixils.crowdcontrol.common.EntityMapper;
import dev.qixils.crowdcontrol.common.PlayerManager;
import dev.qixils.crowdcontrol.common.util.TextUtil;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
public final class FabricCrowdControlPlugin extends AbstractPlugin<ServerPlayerEntity, ServerCommandSource> implements ModInitializer {
	public static boolean CLIENT_INITIALIZED = false;
	public static boolean CLIENT_AVAILABLE = false;
	private static final TextUtil EMPTY_TEXT_UTIL = new TextUtil(null);
	// platform stuff
	@Accessors(fluent = true)
	private @Nullable MinecraftServer server;
	@Accessors(fluent = true)
	private @Nullable FabricServerAudiences adventure;
	private @NotNull TextUtil textUtil = EMPTY_TEXT_UTIL;
	// usual plugin stuff
	private final ExecutorService syncExecutor = Util.getMainWorkerExecutor();
	private final ExecutorService asyncExecutor = Executors.newCachedThreadPool();
	private final CommandRegister register = new CommandRegister(this);
	private final Logger SLF4JLogger = LoggerFactory.getLogger("crowd-control");
	@Accessors(fluent = true)
	private final EntityMapper<ServerPlayerEntity> playerMapper = new PlayerEntityMapper(this);
	@Accessors(fluent = true)
	private final EntityMapper<ServerCommandSource> commandSenderMapper = new ServerCommandSourceMapper(this);
	private final PlayerManager<ServerPlayerEntity> playerManager = new FabricPlayerManager(this);

	public FabricCrowdControlPlugin() {
		super(ServerPlayerEntity.class, ServerCommandSource.class);
	}

	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTING.register(this::setServer);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> setServer(null));
		// TODO render join message
	}

	// boilerplate

	@Override
	public boolean supportsClientOnly() {
		return true;
	}

	@Override
	public Collection<dev.qixils.crowdcontrol.common.Command<ServerPlayerEntity>> registeredCommands() {
		return Collections.unmodifiableCollection(register.getCommands());
	}

	public @NotNull MinecraftServer server() {
		if (this.server == null)
			throw new IllegalStateException("Tried to access server without one running");
		return this.server;
	}

	public @NotNull FabricServerAudiences adventure() {
		if (this.adventure == null)
			throw new IllegalStateException("Tried to access Adventure without running a server");
		return this.adventure;
	}

	private void setServer(@Nullable MinecraftServer server) {
		if (server == null) {
			this.server = null;
			this.adventure = null;
			this.textUtil = EMPTY_TEXT_UTIL;
		} else {
			this.server = server;
			this.adventure = FabricServerAudiences.of(server);
			this.textUtil = new TextUtil(adventure().flattener());
		}
	}
}
