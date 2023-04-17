package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.common.command.Command;
import net.minecraft.resource.featuretoggle.ToggleableFeature;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public interface FeatureElementCommand extends Command<ServerPlayerEntity>, ToggleableFeature {
	@Override
	@NotNull FabricCrowdControlPlugin getPlugin();

	@Override
	default TriState isVisible() {
		return TriState.fromBoolean(getPlugin().isEnabled(this));
	}
}
