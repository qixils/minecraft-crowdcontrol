package dev.qixils.crowdcontrol.common;

import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public interface TimedCommand<P extends Audience> extends VoidCommand<P> {
	@NotNull Duration getDuration();

	default @NotNull String getProcessedDisplayName() {
		return getDisplayName() + " (" + getDuration().toSeconds() + "s)";
	}
}
