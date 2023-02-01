package dev.qixils.crowdcontrol.common.command;

import dev.qixils.crowdcontrol.common.Plugin;
import dev.qixils.crowdcontrol.exceptions.ExceptionUtil;
import dev.qixils.crowdcontrol.socket.Request;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/**
 * A {@link Command} that lasts for a prolonged period of time.
 *
 * @param <P> class used to represent online players
 */
public interface TimedCommand<P> extends Command<P> {

	/**
	 * Gets the default value for how long the effects of this command will last.
	 *
	 * @return default duration of effects
	 */
	@NotNull Duration getDefaultDuration();

	/**
	 * Gets how long the effects of this command will last.
	 *
	 * @return duration of effects
	 */
	default @NotNull Duration getDuration(@NotNull Request request) {
		return ExceptionUtil.validateNotNullElseGet(request.getDuration(), this::getDefaultDuration);
	}

	@Override
	default @NotNull Component getProcessedDisplayName(@NotNull Request request) {
		Component displayName = getDisplayName();
		Duration duration = getDuration(request);
		if (!duration.isZero())
			displayName = displayName.append(Component.text(" (" + duration.getSeconds() + "s)", Plugin.DIM_CMD_COLOR));
		return displayName;
	}
}
