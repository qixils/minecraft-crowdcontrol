package dev.qixils.crowdcontrol.plugin.fabric.commands;

import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

public interface ItemCommand extends FeatureElement {
	@NotNull Item getItem();

	@Override
	default @NotNull FeatureFlagSet requiredFeatures() {
		return getItem().requiredFeatures();
	}
}
