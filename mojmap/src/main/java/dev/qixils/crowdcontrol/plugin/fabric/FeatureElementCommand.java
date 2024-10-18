package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.common.command.Command;
import live.crowdcontrol.cc4j.IUserRecord;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.flag.FeatureElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface FeatureElementCommand extends Command<ServerPlayer>, FeatureElement {
	@Override
	@NotNull ModdedCrowdControlPlugin getPlugin();

	@Override
	default TriState isVisible(@NotNull IUserRecord user, @NotNull List<ServerPlayer> potentialPlayers) {
		return getPlugin().isEnabled(this);
	}
}
