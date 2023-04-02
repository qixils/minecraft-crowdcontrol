package dev.qixils.crowdcontrol.plugin.fabric.commands.executeorperish;

import dev.qixils.crowdcontrol.common.EventListener;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@EventListener
abstract class AbstractListeningCondition<DataType> extends AbstractCondition implements SuccessCondition {
	protected final Map<UUID, DataType> statuses = new HashMap<>();
	protected final @NotNull DataType defaultStatus;

	protected AbstractListeningCondition(int rewardLuck, @NotNull DataType defaultStatus, @Nullable ConditionFlags builder) {
		super(rewardLuck, builder);
		this.defaultStatus = defaultStatus;
	}

	@Override
	public void track(@NotNull UUID player) {
		statuses.put(player, defaultStatus);
	}

	@Override
	public void reset(@NotNull UUID player) {
		statuses.remove(player);
	}

	protected boolean hasStatus(@NotNull UUID player) {
		return statuses.containsKey(player);
	}

	protected boolean hasStatus(@NotNull ServerPlayerEntity player) {
		return hasStatus(player.getUuid());
	}

	protected @NotNull DataType getStatus(@NotNull UUID player) {
		return statuses.getOrDefault(player, defaultStatus);
	}

	protected @NotNull DataType getStatus(@NotNull ServerPlayerEntity player) {
		return getStatus(player.getUuid());
	}

	protected void setStatus(@NotNull UUID player, @NotNull DataType status) {
		statuses.put(player, status);
	}

	protected void setStatus(@NotNull ServerPlayerEntity player, @NotNull DataType status) {
		setStatus(player.getUuid(), status);
	}

	protected void computeStatus(@NotNull UUID player, @NotNull Function<DataType, DataType> modifier) {
		if (!statuses.containsKey(player))
			return;
		statuses.compute(player, (key, value) -> modifier.apply(value));
	}

	protected void computeStatus(@NotNull ServerPlayerEntity player, @NotNull Function<DataType, DataType> modifier) {
		computeStatus(player.getUuid(), modifier);
	}
}
