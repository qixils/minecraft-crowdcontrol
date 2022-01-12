package dev.qixils.crowdcontrol.plugin.paper;

import org.bukkit.entity.Player;

/**
 * A command whose result is available immediately
 */
public abstract class ImmediateCommand extends Command implements dev.qixils.crowdcontrol.common.ImmediateCommand<Player> {
	public ImmediateCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}
}
