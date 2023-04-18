package dev.qixils.crowdcontrol.common;

import dev.qixils.crowdcontrol.CrowdControl;
import dev.qixils.crowdcontrol.common.command.Command;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

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
	protected int port = DEFAULT_PORT;
	@Nullable
	@Getter @Setter
	protected String IP = "127.0.0.1";
	@Getter @Nullable
	protected CrowdControl crowdControl = null;
	@Getter
	protected boolean isServer = true;
	@Getter
	protected boolean global = false;
	protected boolean announce = true;
	@Getter
	protected boolean adminRequired = false;
	@Getter @Setter @NotNull
	protected HideNames hideNames = HideNames.NONE;
	@Getter @NotNull
	protected Collection<String> hosts = Collections.emptySet();
	@Getter @NotNull
	protected LimitConfig limitConfig = new LimitConfig();

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
	public void registerCommand(@NotNull String name, @NotNull Command<P> command) {
		name = name.toLowerCase(Locale.ENGLISH);
		if (crowdControl == null)
			throw new IllegalStateException("CrowdControl is not initialized");
		try {
			crowdControl.registerHandler(name, command::executeAndNotify);
			getSLF4JLogger().debug("Registered CC command '" + name + "'");
		} catch (IllegalArgumentException e) {
			getSLF4JLogger().warn("Failed to register command: " + name, e);
		}
	}
}
