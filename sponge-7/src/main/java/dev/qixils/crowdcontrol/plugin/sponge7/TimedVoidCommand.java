package dev.qixils.crowdcontrol.plugin.sponge7;

import dev.qixils.crowdcontrol.common.command.TimedCommand;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;

public abstract class TimedVoidCommand extends VoidCommand implements TimedCommand<Player> {
	protected TimedVoidCommand(@NotNull SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}
}
