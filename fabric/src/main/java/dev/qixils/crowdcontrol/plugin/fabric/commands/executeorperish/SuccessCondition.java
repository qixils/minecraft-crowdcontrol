package dev.qixils.crowdcontrol.plugin.fabric.commands.executeorperish;

import net.kyori.adventure.text.Component;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;

public interface SuccessCondition {
	@NotNull Component getComponent();

	default boolean canApply(@NotNull ServerPlayerEntity player) {
		return !hasSucceeded(player);
	}

	default boolean canApply(@NotNull Collection<@NotNull ServerPlayerEntity> players) {
		return players.stream().allMatch(this::canApply);
	}

	boolean hasSucceeded(@NotNull ServerPlayerEntity player);

	default void track(@NotNull ServerPlayerEntity player) {
		track(player.getUuid());
	}

	default void track(@NotNull UUID uuid) {
	}

	default void reset(@NotNull ServerPlayerEntity player) {
		reset(player.getUuid());
	}

	default void reset(@NotNull UUID uuid) {
	}

	default int getRewardLuck() {
		return 0;
	}
}
