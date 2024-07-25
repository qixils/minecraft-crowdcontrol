package dev.qixils.crowdcontrol.common.mc;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An entity in the game.
 */
public interface CCEntity {

	/**
	 * Executes some code on this entity's thread.
	 *
	 * @param runnable the code to run
	 * @param onCancel code to run if the scheduled task is cancelled
	 */
	default void execute(@NotNull Runnable runnable, @Nullable Runnable onCancel) {
		runnable.run();
	}

	/**
	 * Executes some code on this entity's thread.
	 *
	 * @param runnable the code to run
	 */
	@ApiStatus.NonExtendable
	default void execute(@NotNull Runnable runnable) {
		execute(runnable, null);
	}

	/**
	 * Kills the entity.
	 */
	void kill();
}
