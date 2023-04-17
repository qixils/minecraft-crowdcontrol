package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.plugin.fabric.FeatureElementCommand;
import net.minecraft.item.Item;
import net.minecraft.resource.featuretoggle.FeatureSet;
import org.jetbrains.annotations.NotNull;

public interface ItemCommand extends FeatureElementCommand {
	@NotNull Item getItem();

	@Override
	default FeatureSet getRequiredFeatures() {
		return getItem().getRequiredFeatures();
	}
}
