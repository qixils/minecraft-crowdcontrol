package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.common.command.Command;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.flag.FeatureElement;
import org.jetbrains.annotations.NotNull;

public interface FeatureElementCommand extends Command<ServerPlayer>, FeatureElement {
	@Override
	@NotNull FabricCrowdControlPlugin getPlugin();

	@Override
	default TriState isVisible() {
		return getPlugin().isEnabled(this);
	}
}
