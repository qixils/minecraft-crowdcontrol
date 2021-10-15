package dev.qixils.crowdcontrol.plugin;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/**
 * A command which has a timed duration
 */
public abstract class TimedCommand extends VoidCommand {
    public TimedCommand(@NotNull CrowdControlPlugin plugin) {
        super(plugin);
    }

    public abstract @NotNull Duration getDuration();

    private String processedDisplayName;

    @Override
    protected @NotNull String getProcessedDisplayName() {
        if (processedDisplayName == null)
            processedDisplayName = getDisplayName() + " (" + getDuration().toSeconds() + "s)";
        return processedDisplayName;
    }
}
