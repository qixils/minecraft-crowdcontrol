package dev.qixils.crowdcontrol.plugin.fabric.mc;

import dev.qixils.crowdcontrol.common.mc.MCCCPlayer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class FabricPlayer extends FabricLivingEntity implements MCCCPlayer {

	public FabricPlayer(ServerPlayer entity) {
		super(entity);
	}

	@Override
	public @NotNull ServerPlayer entity() {
		return (ServerPlayer) super.entity();
	}

	@Override
	public int foodLevel() {
		return entity().getFoodData().getFoodLevel();
	}

	@Override
	public void foodLevel(int foodLevel) {
		entity().getFoodData().setFoodLevel(foodLevel);
	}

	@Override
	public double saturation() {
		return entity().getFoodData().getSaturationLevel();
	}

	@Override
	public void saturation(double saturation) {
		entity().getFoodData().setSaturation((float) saturation);
	}

	@Override
	public int xpLevel() {
		return entity().experienceLevel;
	}

	@Override
	public void xpLevel(int xpLevel) {
		entity().setExperienceLevels(xpLevel);
	}

	@Override
	public void addXpLevel(int xpLevel) {
		entity().giveExperienceLevels(xpLevel);
	}
}
