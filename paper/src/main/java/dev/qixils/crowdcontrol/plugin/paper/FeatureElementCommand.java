package dev.qixils.crowdcontrol.plugin.paper;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.common.command.Command;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface FeatureElementCommand extends Command<Player> {

	default boolean isFeatureEnabled(@NotNull World world) {
		return true;
	}

	@Override
	default TriState isVisible() {
		return TriState.fromBoolean(PaperCrowdControlPlugin.isFeatureEnabled(this));
	}
}
