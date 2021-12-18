package dev.qixils.crowdcontrol.common;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public interface TimedCommand<P> extends VoidCommand<P> {
	@NotNull Duration getDuration();

	default @NotNull String getProcessedDisplayName() {
		return getDisplayName() + " (" + getDuration().getSeconds() + "s)";
	}
}
