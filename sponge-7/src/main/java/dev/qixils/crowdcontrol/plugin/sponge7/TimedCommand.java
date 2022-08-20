package dev.qixils.crowdcontrol.plugin.sponge7;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;

public abstract class TimedCommand extends Command implements dev.qixils.crowdcontrol.common.command.TimedCommand<Player> {
	protected TimedCommand(@NotNull SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}
}
