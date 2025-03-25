package dev.qixils.crowdcontrol.plugin.paper;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.common.command.Command;
import live.crowdcontrol.cc4j.IUserRecord;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface FeatureElementCommand extends Command<Player> {

	default boolean isFeatureEnabled(@NotNull World world) {
		return true;
	}

	@Override
	default TriState isVisible(@NotNull IUserRecord user, @NotNull List<Player> potentialPlayers) {
		return TriState.fromBoolean(PaperCrowdControlPlugin.isFeatureEnabled(this));
	}
}
