package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.plugin.fabric.FeatureElementCommand;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

public interface ItemCommand extends FeatureElementCommand {
	@NotNull Item getItem();

	@Override
	default @NotNull FeatureFlagSet requiredFeatures() {
		return getItem().requiredFeatures();
	}
}
