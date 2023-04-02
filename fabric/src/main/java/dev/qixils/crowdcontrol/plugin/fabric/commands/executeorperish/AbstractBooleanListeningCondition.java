package dev.qixils.crowdcontrol.plugin.fabric.commands.executeorperish;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class AbstractBooleanListeningCondition extends AbstractListeningCondition<Boolean> {
	protected AbstractBooleanListeningCondition(int rewardLuck, @Nullable ConditionFlags builder) {
		super(rewardLuck, false, builder);
	}

	@Override
	public boolean hasSucceeded(@NotNull ServerPlayerEntity player) {
		return getStatus(player);
	}
}
