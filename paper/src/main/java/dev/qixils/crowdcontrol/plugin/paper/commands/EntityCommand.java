package dev.qixils.crowdcontrol.plugin.paper.commands;

import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlagSet;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftMagicNumbers;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public interface EntityCommand extends FeatureElement {
	@NotNull EntityType getEntityType();

	@Override
	default @NotNull FeatureFlagSet requiredFeatures() {
		return CraftMagicNumbers.getEntityTypes(getEntityType()).requiredFeatures();
	}
}
