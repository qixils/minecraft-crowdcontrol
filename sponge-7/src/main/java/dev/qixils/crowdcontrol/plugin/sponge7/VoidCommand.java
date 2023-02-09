package dev.qixils.crowdcontrol.plugin.sponge7;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;

public abstract class VoidCommand extends Command implements dev.qixils.crowdcontrol.common.command.VoidCommand<Player> {
	protected VoidCommand(@NotNull SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}
}
