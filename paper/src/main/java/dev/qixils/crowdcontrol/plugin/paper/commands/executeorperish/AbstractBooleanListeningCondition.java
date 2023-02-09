package dev.qixils.crowdcontrol.plugin.paper.commands.executeorperish;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class AbstractBooleanListeningCondition extends AbstractListeningCondition<Boolean> {
	protected AbstractBooleanListeningCondition(int rewardLuck, @Nullable ConditionFlags builder) {
		super(rewardLuck, false, builder);
	}

	@Override
	public boolean hasSucceeded(@NotNull Player player) {
		return getStatus(player);
	}
}
