package dev.qixils.crowdcontrol.common.command;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/**
 * A {@link Command} that lasts for a prolonged period of time.
 *
 * @param <P> class used to represent online players
 */
public interface TimedCommand<P> extends VoidCommand<P> {

	/**
	 * Gets how long the effects of this command will last.
	 *
	 * @return duration of effects
	 */
	@NotNull Duration getDuration();

	@Override
	default @NotNull String getProcessedDisplayName() {
		return getDisplayName() + " (" + getDuration().getSeconds() + "s)";
	}
}
