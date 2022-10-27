package dev.qixils.crowdcontrol.plugin.paper.commands.executeorperish;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;

public interface SuccessCondition {
	@NotNull Component getComponent();

	default boolean canApply(@NotNull Player player) {
		return !hasSucceeded(player);
	}

	default boolean canApply(@NotNull Collection<@NotNull Player> players) {
		return players.stream().allMatch(this::canApply);
	}

	boolean hasSucceeded(@NotNull Player player);

	default void track(@NotNull Player player) {
		track(player.getUniqueId());
	}

	default void track(@NotNull UUID uuid) {
	}

	default void reset(@NotNull Player player) {
		reset(player.getUniqueId());
	}

	default void reset(@NotNull UUID uuid) {
	}

	default int getRewardLuck() {
		return 0;
	}
}
