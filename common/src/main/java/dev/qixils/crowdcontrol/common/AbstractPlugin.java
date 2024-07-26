package dev.qixils.crowdcontrol.common;

import dev.qixils.crowdcontrol.CrowdControl;
import dev.qixils.crowdcontrol.common.command.Command;
import dev.qixils.crowdcontrol.common.util.SemVer;
import dev.qixils.crowdcontrol.socket.SocketManager;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Abstraction layer for {@link Plugin} which implements platform-agnostic methods.
 *
 * @param <P> class used to represent online players
 * @param <S> class used to represent command senders in Cloud Command Framework
 */
public abstract class AbstractPlugin<P, S> implements Plugin<P, S> {

	@Getter @NotNull
	private final Class<P> playerClass;
	@Getter @NotNull
	private final Class<S> commandSenderClass;
	@Nullable
	@Getter @Setter
	protected String password = DEFAULT_PASSWORD;
	@Getter @Setter
	protected String IP = null;
	@Getter @Setter
	protected int port = DEFAULT_PORT;
	@Getter @Nullable
	protected CrowdControl crowdControl = null;
	@Getter
	protected boolean global = false;
	protected boolean announce = true;
	@Getter
	protected boolean adminRequired = false;
	@Getter
	protected boolean autoDetectIP = true;
	@Getter @Setter @NotNull
	protected HideNames hideNames = HideNames.NONE;
	@Getter @NotNull
	protected Collection<String> hosts = Collections.emptySet();
	@Getter @NotNull
	protected LimitConfig limitConfig = new LimitConfig();
	@Getter @NotNull
	protected SoftLockConfig softLockConfig = new SoftLockConfig();
	@Getter @NotNull
	protected final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
	@Getter @NotNull
	protected final Map<String, List<SocketManager>> sentEvents = new HashMap<>();
	protected final Map<UUID, SemVer> clientVersions = new HashMap<>();

	protected AbstractPlugin(@NotNull Class<P> playerClass, @NotNull Class<S> commandSenderClass) {
		this.playerClass = playerClass;
		this.commandSenderClass = commandSenderClass;
	}

	@Override
	public boolean announceEffects() {
		return announce;
	}

	@Override
	public void setAnnounceEffects(boolean announce) {
		this.announce = announce;
	}

	@Override
	public void updateCrowdControl(@Nullable CrowdControl crowdControl) {
		this.crowdControl = crowdControl;
	}

	@Override
	public void registerCommand(@Nullable String name, @NotNull Command<P> command) {
		if (name != null)
			name = name.toLowerCase(Locale.ENGLISH);
		if (crowdControl == null)
			throw new IllegalStateException("CrowdControl is not initialized");
		try {
			crowdControl.registerHandler(name, command::executeAndNotify);
			getSLF4JLogger().debug("Registered CC command '{}'", name);
		} catch (IllegalArgumentException e) {
			getSLF4JLogger().warn("Failed to register command: {}", name, e);
		}
	}

	@Override
	public @NotNull Optional<SemVer> getModVersion(@NotNull P player) {
		return Optional.ofNullable(clientVersions.get(playerMapper().getUniqueId(player)));
	}

	@Override
	public int getModdedPlayerCount() {
		return clientVersions.size();
	}

	@Override
	public void onPlayerLeave(P player) {
		clientVersions.remove(playerMapper().getUniqueId(player));
		Plugin.super.onPlayerLeave(player);
	}
}
