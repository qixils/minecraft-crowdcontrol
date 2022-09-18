package dev.qixils.crowdcontrol.plugin.paper;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * A command which has a timed duration
 */
public abstract class TimedCommand extends VoidCommand implements dev.qixils.crowdcontrol.common.command.TimedCommand<Player> {
	public TimedCommand(@NotNull PaperCrowdControlPlugin plugin) {
		super(plugin);
	}
}
