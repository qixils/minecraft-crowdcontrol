package dev.qixils.crowdcontrol.plugin.fabric.commands;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlagSet;
import org.jetbrains.annotations.NotNull;

public interface EntityCommand<E extends Entity> extends FeatureElement {
	@NotNull EntityType<E> getEntityType();

	@Override
	default @NotNull FeatureFlagSet requiredFeatures() {
		return getEntityType().requiredFeatures();
	}
}
