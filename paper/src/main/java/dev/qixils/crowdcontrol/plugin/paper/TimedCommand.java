package dev.qixils.crowdcontrol.plugin.paper;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * A command which has a timed duration
 */
public abstract class TimedCommand extends VoidCommand implements dev.qixils.crowdcontrol.common.command.TimedCommand<Player> {
	private String processedDisplayName;

	public TimedCommand(@NotNull PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public @NotNull String getProcessedDisplayName() {
		if (processedDisplayName == null)
			processedDisplayName = dev.qixils.crowdcontrol.common.command.TimedCommand.super.getProcessedDisplayName();
		return processedDisplayName;
	}
}
