package dev.qixils.crowdcontrol.plugin.paper.commands.executeorperish;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public interface SuccessCondition {
	Component getComponent();

	boolean hasSucceeded(Player player);

	// TODO: boolean hasSucceeded(Event event);

	default int getRewardLuck() {
		return 0;
	}
}
