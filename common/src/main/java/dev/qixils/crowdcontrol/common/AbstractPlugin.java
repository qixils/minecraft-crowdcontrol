package dev.qixils.crowdcontrol.common;

import dev.qixils.crowdcontrol.CrowdControl;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

public abstract class AbstractPlugin<P extends S, S> implements Plugin<P, S> {
	@Getter
	private final Class<P> playerClass;
	@Getter
	private final Class<S> commandSenderClass;
	protected String manualPassword = null;
	@Getter
	protected CrowdControl crowdControl = null;
	@Getter
	protected boolean isServer = true;
	@Getter
	protected boolean global = false;
	protected boolean announce = true;
	@Getter
	protected Collection<String> hosts = Collections.emptyList();

	public AbstractPlugin(@NotNull Class<P> playerClass, @NotNull Class<S> commandSenderClass) {
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
		crowdControl.registerHandler(name, command::executeAndNotify);
		getSLF4JLogger().debug("Registered CC command '" + name + "'");
	}
}