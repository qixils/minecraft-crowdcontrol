package dev.qixils.crowdcontrol.plugin.fabric.commands.executeorperish;

import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerPlayer;

public interface SuccessCondition {
	Component getComponent();

	boolean hasSucceeded(ServerPlayer player);

	// TODO: boolean hasSucceeded(Event event);

	default int getRewardLuck() {
		return 0;
	}
}
