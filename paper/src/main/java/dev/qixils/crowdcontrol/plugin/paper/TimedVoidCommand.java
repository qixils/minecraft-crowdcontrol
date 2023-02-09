package dev.qixils.crowdcontrol.plugin.paper;

import dev.qixils.crowdcontrol.common.command.TimedCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class TimedVoidCommand extends VoidCommand implements TimedCommand<Player> {
	public TimedVoidCommand(@NotNull PaperCrowdControlPlugin plugin) {
		super(plugin);
	}
}
