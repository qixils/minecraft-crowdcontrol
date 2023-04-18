package dev.qixils.crowdcontrol.plugin.paper.scheduling;

import dev.qixils.crowdcontrol.common.scheduling.AgnosticTask;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static dev.qixils.crowdcontrol.common.util.Utils.executeOn;

/**
 * Utilities for Folia executors.
 */
public class FoliaExecutors {

	/**
	 * Gets a task from a reference, or creates a new one.
	 *
	 * @param taskRef reference to task
	 * @param taskFactory factory for creating agnostic tasks from folia tasks
	 * @param foliaTask folia task
	 * @return task
	 */
	@NotNull
	public static <P, B> P getTask(@NotNull AtomicReference<@Nullable P> taskRef, @NotNull Function<@Nullable B, @NotNull P> taskFactory, @Nullable B foliaTask) {
		P get = taskRef.get();
		if (get == null) {
			get = taskFactory.apply(foliaTask);
			taskRef.set(get);
		}
		return get;
	}

	/**
	 * Gets a task from a reference, or creates a new one.
	 *
	 * @param taskRef reference to task
	 * @param foliaTask folia task
	 * @return task
	 */
	@NotNull
	public static AgnosticTask getTask(@NotNull AtomicReference<@Nullable AgnosticTask> taskRef, @Nullable ScheduledTask foliaTask) {
		return getTask(taskRef, FoliaTask::new, foliaTask);
	}

	/**
	 * Gets a task from a reference, or creates a new one.
	 *
	 * @param taskRef reference to task
	 * @param bukkitTask bukkit task
	 * @return task
	 */
	@NotNull
	public static AgnosticTask getTask(@NotNull AtomicReference<@Nullable AgnosticTask> taskRef, @Nullable BukkitTask bukkitTask) {
		return getTask(taskRef, PaperTask::new, bukkitTask);
	}

	/**
	 * Creates an {@code onRetire} runnable that marks a task as retired.
	 *
	 * @param taskRef reference to task
	 * @return runnable
	 */
	@NotNull
	public static Runnable getOnRetireRunnable(@NotNull AtomicReference<@Nullable FoliaTask> taskRef) {
		return () -> executeOn(taskRef.get(), FoliaTask::retire);
	}
}
