package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.plugin.fabric.FeatureElementCommand;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.flag.FeatureFlagSet;
import org.jetbrains.annotations.NotNull;

public interface EntityCommand<E extends Entity> extends FeatureElementCommand {
	@NotNull EntityType<E> getEntityType();

	@Override
	default @NotNull FeatureFlagSet requiredFeatures() {
		return getEntityType().requiredFeatures();
	}

	@Override
	default TriState isSelectable() {
		if (getEntityType().getCategory() != MobCategory.MONSTER)
			return TriState.TRUE;
		if (getPlugin().server().getWorldData().getDifficulty() != Difficulty.PEACEFUL)
			return TriState.TRUE;
		return TriState.FALSE;
	}
}
