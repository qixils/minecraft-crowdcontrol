package dev.qixils.crowdcontrol.plugin.paper.mc;

import dev.qixils.crowdcontrol.common.mc.MCCCPlayer;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PaperPlayer extends PaperLivingEntity implements MCCCPlayer {

	public PaperPlayer(@NotNull PaperCrowdControlPlugin plugin, @NotNull Player player) {
		super(plugin, player);
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
	public void foodLevel(int foodLevel) {
		execute(() -> entity().setFoodLevel(foodLevel));
	}

	@Override
	public double saturation() {
		return entity().getSaturation();
	}

	@Override
	public void saturation(double saturation) {
		execute(() -> entity().setSaturation((float) saturation));
	}

	@Override
	public int xpLevel() {
		return entity().getLevel();
	}

	@Override
	public void xpLevel(int xpLevel) {
		execute(() -> entity().setLevel(xpLevel));
	}

	@Override
	public void addXpLevel(int xpLevel) {
		execute(() -> entity().giveExpLevels(xpLevel));
	}
}
