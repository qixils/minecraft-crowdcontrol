package dev.qixils.crowdcontrol.plugin.paper.commands.executeorperish;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

abstract class AbstractCondition implements SuccessCondition {
	protected final int rewardLuck;
	protected final @NotNull ConditionFlags flags;

	protected AbstractCondition(int rewardLuck, @Nullable ConditionFlags builder) {
		this.rewardLuck = rewardLuck;
		flags = Objects.requireNonNullElse(builder, ConditionFlags.DEFAULT);
	}

	@Override
	public boolean canApply(@NotNull Player player) {
		return !hasSucceeded(player) && flags.test(player);
	}

	@Override
	public int getRewardLuck() {
		return rewardLuck;
	}
}
