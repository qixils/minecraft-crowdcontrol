package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.FeatureElementCommand;
import net.minecraft.world.flag.FeatureFlagSet;
import org.bukkit.Material;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.jetbrains.annotations.NotNull;

public interface ItemCommand extends FeatureElementCommand {
	@NotNull Material getItem();

	@Override
	default @NotNull FeatureFlagSet requiredFeatures() {
		return CraftMagicNumbers.getItem(getItem()).requiredFeatures();
	}
}
