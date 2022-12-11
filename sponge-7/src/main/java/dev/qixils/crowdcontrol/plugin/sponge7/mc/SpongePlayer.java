package dev.qixils.crowdcontrol.plugin.sponge7.mc;

import dev.qixils.crowdcontrol.common.mc.CCPlayer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;

public class SpongePlayer extends SpongeLivingEntity implements CCPlayer {

	public SpongePlayer(Player player) {
		super(player);
	}

	@Override
	public @NotNull Player entity() {
		return (Player) super.entity();
	}

	@Override
	public int foodLevel() {
		return entity().foodLevel().get();
	}

	@Override
	public double saturation() {
		return entity().saturation().get();
	}

	@Override
	public void foodLevel(int foodLevel) {
		entity().offer(Keys.FOOD_LEVEL, foodLevel);
	}

	@Override
	public void saturation(double saturation) {
		entity().offer(Keys.SATURATION, saturation);
	}
}
