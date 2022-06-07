package dev.qixils.crowdcontrol.common;

import dev.qixils.crowdcontrol.CrowdControl;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
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
	protected String manualPassword = null;
	@Getter @Nullable
	protected CrowdControl crowdControl = null;
	@Getter
	protected boolean isServer = true;
	@Getter
	protected boolean global = false;
	protected boolean announce = true;
	@Getter @NotNull
	protected Collection<String> hosts = Collections.emptyList();
	@Getter @NotNull
	protected final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

	protected AbstractPlugin(@NotNull Class<P> playerClass, @NotNull Class<S> commandSenderClass) {
		this.playerClass = playerClass;
		this.commandSenderClass = commandSenderClass;
	}

	@Override
	public boolean announceEffects() {
		return announce;
	}

	@Override
	public void updateCrowdControl(@Nullable CrowdControl crowdControl) {
		this.crowdControl = crowdControl;
	}

	@Override
	public void setPassword(@NotNull String password) throws IllegalArgumentException, IllegalStateException {
		if (!isServer())
			throw new IllegalStateException("Not running in server mode");
		manualPassword = password;
	}

	@Override
	public void registerCommand(@NotNull String name, @NotNull Command<P> command) {
		name = name.toLowerCase(Locale.ENGLISH);
		if (crowdControl == null)
			throw new IllegalStateException("CrowdControl is not initialized");
		crowdControl.registerHandler(name, command::executeAndNotify);
		getSLF4JLogger().debug("Registered CC command '" + name + "'");
	}
}
