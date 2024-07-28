package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.FeatureElementCommand;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public interface ItemCommand extends FeatureElementCommand {
	@NotNull Material getItem();

	@Override
	default boolean isFeatureEnabled(@NotNull World world) {
		return getItem().isEnabledByFeature(world);
	}
}
