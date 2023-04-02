package dev.qixils.crowdcontrol.plugin.fabric.mc;

import dev.qixils.crowdcontrol.common.mc.CCPlayer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class FabricPlayer extends FabricLivingEntity implements CCPlayer {

	public FabricPlayer(ServerPlayerEntity entity) {
		super(entity);
	}

	@Override
	public @NotNull ServerPlayerEntity entity() {
		return (ServerPlayerEntity) super.entity();
	}

	@Override
	public int foodLevel() {
		return entity().getHungerManager().getFoodLevel();
	}

	@Override
	public void foodLevel(int foodLevel) {
		entity().getHungerManager().setFoodLevel(foodLevel);
	}

	@Override
	public double saturation() {
		return entity().getHungerManager().getSaturationLevel();
	}

	@Override
	public void saturation(double saturation) {
		entity().getHungerManager().setSaturationLevel((float) saturation);
	}

	@Override
	public int xpLevel() {
		return entity().experienceLevel;
	}

	@Override
	public void xpLevel(int xpLevel) {
		entity().setExperienceLevel(xpLevel);
	}

	@Override
	public void addXpLevel(int xpLevel) {
		entity().addExperienceLevels(xpLevel);
	}
}
