package dev.qixils.crowdcontrol.plugin.paper;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.common.command.Command;
import org.bukkit.entity.Player;

import java.util.Optional;

public interface FeatureElementCommand extends Command<Player> {
	Optional<Object> requiredFeatures();

	@Override
	default TriState isVisible() {
		return TriState.fromBoolean(PaperCrowdControlPlugin.isFeatureEnabled(this));
	}
}
