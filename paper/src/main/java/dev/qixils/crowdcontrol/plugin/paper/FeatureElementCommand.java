package dev.qixils.crowdcontrol.plugin.paper;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.common.command.Command;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlagSet;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface FeatureElementCommand extends Command<Player>, FeatureElement {
	@Override
	default @NotNull FeatureFlagSet requiredFeatures() {
		return FeatureFlagSet.of();
	}

	@Override
	default TriState isVisible() {
		return TriState.fromBoolean(PaperCrowdControlPlugin.isFeatureEnabled(this));
	}
}
