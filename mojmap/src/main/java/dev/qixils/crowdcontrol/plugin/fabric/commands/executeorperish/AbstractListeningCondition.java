package dev.qixils.crowdcontrol.plugin.fabric.commands.executeorperish;

import dev.qixils.crowdcontrol.common.EventListener;
import net.minecraft.server.level.ServerPlayer;
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

	protected boolean hasStatus(@NotNull ServerPlayer player) {
		return hasStatus(player.getUUID());
	}

	protected @NotNull DataType getStatus(@NotNull UUID player) {
		return statuses.getOrDefault(player, defaultStatus);
	}

	protected @NotNull DataType getStatus(@NotNull ServerPlayer player) {
		return getStatus(player.getUUID());
	}

	protected void setStatus(@NotNull UUID player, @NotNull DataType status) {
		statuses.put(player, status);
	}

	protected void setStatus(@NotNull ServerPlayer player, @NotNull DataType status) {
		setStatus(player.getUUID(), status);
	}

	protected void computeStatus(@NotNull UUID player, @NotNull Function<DataType, DataType> modifier) {
		if (!statuses.containsKey(player))
			return;
		statuses.compute(player, (key, value) -> modifier.apply(value));
	}

	protected void computeStatus(@NotNull ServerPlayer player, @NotNull Function<DataType, DataType> modifier) {
		computeStatus(player.getUUID(), modifier);
	}
}
