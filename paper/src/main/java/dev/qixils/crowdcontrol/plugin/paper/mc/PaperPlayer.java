package dev.qixils.crowdcontrol.plugin.paper.mc;

import dev.qixils.crowdcontrol.common.mc.CCPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PaperPlayer extends PaperLivingEntity implements CCPlayer {

	public PaperPlayer(Player player) {
		super(player);
	}

	@Override
	public @NotNull Player entity() {
		return (Player) super.entity();
	}

	@Override
	public int foodLevel() {
		return entity().getFoodLevel();
	}

	@Override
	public double saturation() {
		return entity().getSaturation();
	}

	@Override
	public void foodLevel(int foodLevel) {
		entity().setFoodLevel(foodLevel);
	}

	@Override
	public void saturation(double saturation) {
		entity().setSaturation((float) saturation);
	}
}
