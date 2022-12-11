package dev.qixils.crowdcontrol.plugin.fabric.mc;

import dev.qixils.crowdcontrol.common.mc.CCPlayer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class FabricPlayer extends FabricLivingEntity implements CCPlayer {

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
	public double saturation() {
		return entity().getFoodData().getSaturationLevel();
	}

	@Override
	public void foodLevel(int foodLevel) {
		entity().getFoodData().setFoodLevel(foodLevel);
	}

	@Override
	public void saturation(double saturation) {
		entity().getFoodData().setSaturation((float) saturation);
	}
}
