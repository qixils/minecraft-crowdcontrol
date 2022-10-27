package dev.qixils.crowdcontrol.plugin.paper.commands.executeorperish;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

abstract class AbstractListeningCondition extends AbstractCondition implements SuccessCondition, Listener {
	protected final Map<UUID, Boolean> statuses = new HashMap<>();

	protected AbstractListeningCondition(int rewardLuck, @Nullable ConditionFlags builder) {
		super(rewardLuck, builder);
	}

	@Override
	public void track(@NotNull UUID player) {
		statuses.put(player, false);
	}

	@Override
	public void reset(@NotNull UUID player) {
		statuses.remove(player);
	}

	@Override
	public boolean hasSucceeded(@NotNull Player player) {
		return statuses.getOrDefault(player.getUniqueId(), false);
	}
}
