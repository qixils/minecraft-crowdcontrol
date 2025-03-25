package dev.qixils.crowdcontrol.plugin.fabric.commands.executeorperish;

import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;

public interface SuccessCondition {
	@NotNull Component getComponent();

	default boolean canApply(@NotNull ServerPlayer player) {
		return !hasSucceeded(player);
	}

	default boolean canApply(@NotNull Collection<@NotNull ServerPlayer> players) {
		return players.stream().allMatch(this::canApply);
	}

	boolean hasSucceeded(@NotNull ServerPlayer player);

	default void track(@NotNull ServerPlayer player) {
		track(player.getUUID());
	}

	default void track(@NotNull UUID uuid) {
	}

	default void reset(@NotNull ServerPlayer player) {
		reset(player.getUUID());
	}

	default void reset(@NotNull UUID uuid) {
	}

	default int getRewardLuck() {
		return 0;
	}
}
