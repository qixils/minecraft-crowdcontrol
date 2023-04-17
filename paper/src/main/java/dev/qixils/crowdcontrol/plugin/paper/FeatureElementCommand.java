package dev.qixils.crowdcontrol.plugin.paper;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.common.command.Command;
import net.minecraft.world.flag.FeatureElement;
import org.bukkit.entity.Player;

public interface FeatureElementCommand extends Command<Player>, FeatureElement {
	@Override
	default TriState isVisible() {
		return TriState.fromBoolean(PaperCrowdControlPlugin.isEnabled(this));
	}
}
