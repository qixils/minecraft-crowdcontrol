package dev.qixils.crowdcontrol.plugin.fabric.commands;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.resource.featuretoggle.ToggleableFeature;
import org.jetbrains.annotations.NotNull;

public interface EntityCommand<E extends Entity> extends ToggleableFeature {
	@NotNull EntityType<E> getEntityType();

	@Override
	default @NotNull FeatureSet getRequiredFeatures() {
		return getEntityType().getRequiredFeatures();
	}
}
