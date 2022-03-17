package dev.qixils.crowdcontrol.mojmap;

import dev.qixils.crowdcontrol.common.AbstractPlugin;
import dev.qixils.crowdcontrol.common.EntityMapper;
import dev.qixils.crowdcontrol.common.PlayerManager;
import dev.qixils.crowdcontrol.common.util.TextUtil;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.platform.AudienceProvider;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The main class used by a Crowd Control implementation based on the decompiled code of Minecraft
 * for managing Crowd Control server/client connections and handling {@link Command}s.
 */
@Getter
public abstract class MojmapPlugin extends AbstractPlugin<ServerPlayer, CommandSourceStack> {
	private static final TextUtil EMPTY_TEXT_UTIL = new TextUtil(null);
	private final CommandRegister register = new CommandRegister(this);
	@Nullable
	protected MinecraftServer server;
	@Nullable @Accessors(fluent = true)
	protected AudienceProvider adventure;
	@NotNull
	private TextUtil textUtil = EMPTY_TEXT_UTIL;
	// TODO is this actually the sync executor?? 'Main' sounds sync but 'background' doesn't
	private final ExecutorService syncExecutor = Util.backgroundExecutor();
	private final ExecutorService asyncExecutor = Executors.newCachedThreadPool();
	private final Logger SLF4JLogger = LoggerFactory.getLogger("crowd-control");
	private final PlayerManager<ServerPlayer> playerManager = new MojmapPlayerManager(this);
	@Accessors(fluent = true)
	private final EntityMapper<ServerPlayer> playerMapper = new PlayerEntityMapper(this);

	protected MojmapPlugin() {
		super(ServerPlayer.class, CommandSourceStack.class);
	}

	@Override
	public boolean supportsClientOnly() {
		return true;
	}

	@Override
	public Collection<dev.qixils.crowdcontrol.common.Command<ServerPlayer>> registeredCommands() {
		return Collections.unmodifiableCollection(register.getCommands());
	}

	@NotNull
	public MinecraftServer server() throws IllegalStateException {
		if (this.server == null)
			throw new IllegalStateException("Tried to access server without one running");
		return this.server;
	}

	@NotNull
	public AudienceProvider adventure() throws IllegalStateException {
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
			this.adventure = initAdventure(server);
			this.textUtil = new TextUtil(this.adventure.flattener());
		}
	}
}
