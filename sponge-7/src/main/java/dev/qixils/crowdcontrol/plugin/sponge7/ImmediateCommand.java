package dev.qixils.crowdcontrol.plugin.sponge7;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;

public abstract class ImmediateCommand extends Command implements dev.qixils.crowdcontrol.common.command.ImmediateCommand<Player> {
	protected ImmediateCommand(@NotNull SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}
}
