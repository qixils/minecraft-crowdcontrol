package dev.qixils.crowdcontrol.plugin.sponge8.commands.executeorperish;

import net.kyori.adventure.text.Component;
import org.spongepowered.api.entity.living.player.Player;

public interface SuccessCondition {
	Component getComponent();

	boolean hasSucceeded(Player player);

	// TODO: boolean hasSucceeded(Event event);

	default int getRewardLuck() {
		return 0;
	}
}
