package dev.qixils.crowdcontrol.plugin.sponge7.mc;

import dev.qixils.crowdcontrol.common.mc.CCPlayer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;

public class SpongePlayer extends SpongeLivingEntity implements CCPlayer {

	public SpongePlayer(Player player) {
		super(player);
	}

	@Override
	public @NotNull Player entity() {
		return (Player) super.entity();
	}
}
