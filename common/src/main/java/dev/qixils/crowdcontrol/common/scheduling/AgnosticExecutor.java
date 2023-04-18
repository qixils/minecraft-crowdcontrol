package dev.qixils.crowdcontrol.common.scheduling;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * An executor that can run tasks on a thread or set of threads.
 */
public interface AgnosticExecutor extends Executor {

	/**
	 * Schedules a task to be run on this executor.
	 *
	 * @param runnable the task to run
	 */
	@Override
	default void execute(@NotNull Runnable runnable) {
		run(runnable);
	}

	/**
	 * Schedules a task to be run on this executor.
	 *
	 * @param consumer the task to run
	 * @return the task
	 */
	@NotNull
	AgnosticTask run(@NotNull Consumer<AgnosticTask> consumer);

	/**
	 * Schedules a task to be run on this executor.
	 *
	 * @param runnable the task to run
	 * @return the task
	 */
	@NotNull
	default AgnosticTask run(@NotNull Runnable runnable) {
		return run(task -> runnable.run());
	}

	/**
	 * Schedules a task to run after a delay in ticks.
	 * Delay may be approximate if this executor is asynchronous.
	 *
	 * @param delay the delay in ticks
	 * @param consumer the task to run
	 * @return the task
	 */
	@NotNull
	AgnosticTask runLater(long delay, @NotNull Consumer<AgnosticTask> consumer);

	/**
	 * Schedules a task to run after a delay.
	 * Delay may be approximate if this executor is tick-based.
	 *
	 * @param delay the delay
	 * @param consumer the task to run
	 * @return the task
	 */
	@NotNull
	AgnosticTask runLater(@NotNull Duration delay, @NotNull Consumer<AgnosticTask> consumer);

	/**
	 * Schedules a task to run after a delay in ticks.
	 * Delay may be approximate if this executor is asynchronous.
	 *
	 * @param delay the delay in ticks
	 * @param runnable the task to run
	 * @return the task
	 */
	@NotNull
	default AgnosticTask runLater(long delay, @NotNull Runnable runnable) {
		return runLater(delay, task -> runnable.run());
	}

	/**
	 * Schedules a task to run after a delay.
	 * Delay may be approximate if this executor is tick-based.
	 *
	 * @param delay the delay
	 * @param runnable the task to run
	 * @return the task
	 */
	@NotNull
	default AgnosticTask runLater(@NotNull Duration delay, @NotNull Runnable runnable) {
		return runLater(delay, task -> runnable.run());
	}

	/**
	 * Schedules a task to run repeatedly after a delay in ticks.
	 * Delay may be approximate if this executor is asynchronous.
	 *
	 * @param delay the delay in ticks
	 * @param period the period in ticks
	 * @param consumer the task to run
	 * @return the task
	 */
	@NotNull
	AgnosticTask runRepeating(long delay, long period, @NotNull Consumer<AgnosticTask> consumer);

	/**
	 * Schedules a task to run repeatedly after a delay.
	 * Delay may be approximate if this executor is tick-based
	 *
	 * @param delay the delay
	 * @param period the period
	 * @param consumer the task to run
	 * @return the task
	 */
	@NotNull
	AgnosticTask runRepeating(@NotNull Duration delay, @NotNull Duration period, @NotNull Consumer<AgnosticTask> consumer);

	/**
	 * Schedules a task to run repeatedly after a delay in ticks.
	 * Delay may be approximate if this executor is asynchronous.
	 *
	 * @param delay the delay in ticks
	 * @param period the period in ticks
	 * @param runnable the task to run
	 * @return the task
	 */
	@NotNull
	default AgnosticTask runRepeating(long delay, long period, @NotNull Runnable runnable) {
		return runRepeating(delay, period, task -> runnable.run());
	}

	/**
	 * Schedules a task to run repeatedly after a delay.
	 * Delay may be approximate if this executor is tick-based
	 *
	 * @param delay the delay
	 * @param period the period
	 * @param runnable the task to run
	 * @return the task
	 */
	@NotNull
	default AgnosticTask runRepeating(@NotNull Duration delay, @NotNull Duration period, @NotNull Runnable runnable) {
		return runRepeating(delay, period, task -> runnable.run());
	}
}
