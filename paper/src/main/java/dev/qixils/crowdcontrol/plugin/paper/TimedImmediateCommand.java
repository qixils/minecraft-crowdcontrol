package dev.qixils.crowdcontrol.plugin.paper;

import dev.qixils.crowdcontrol.common.command.TimedCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class TimedImmediateCommand extends Command implements TimedCommand<Player>, dev.qixils.crowdcontrol.common.command.ImmediateCommand<Player> {
	protected TimedImmediateCommand(@NotNull PaperCrowdControlPlugin plugin) {
		super(plugin);
	}
}
