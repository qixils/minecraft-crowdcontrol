package dev.qixils.crowdcontrol.plugin.paper.commands;

import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlagSet;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftMagicNumbers;
import org.jetbrains.annotations.NotNull;

public interface ItemCommand extends FeatureElement {
	@NotNull Material getItem();

	@Override
	default @NotNull FeatureFlagSet requiredFeatures() {
		return CraftMagicNumbers.getItem(getItem()).requiredFeatures();
	}
}
