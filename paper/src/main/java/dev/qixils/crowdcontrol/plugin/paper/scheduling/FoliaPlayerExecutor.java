package dev.qixils.crowdcontrol.plugin.paper.scheduling;

import dev.qixils.crowdcontrol.common.scheduling.AgnosticExecutor;
import dev.qixils.crowdcontrol.common.scheduling.AgnosticTask;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static dev.qixils.crowdcontrol.plugin.paper.scheduling.FoliaExecutors.getOnRetireRunnable;
import static dev.qixils.crowdcontrol.plugin.paper.scheduling.FoliaExecutors.getTask;

public record FoliaPlayerExecutor(PaperCrowdControlPlugin plugin, EntityScheduler scheduler) implements AgnosticExecutor {

	public FoliaPlayerExecutor(PaperCrowdControlPlugin plugin, Player player) {
		this(plugin, player.getScheduler());
	}

	@Override
	public @NotNull AgnosticTask run(@NotNull Consumer<AgnosticTask> consumer) {
		AtomicReference<FoliaTask> taskRef = new AtomicReference<>();
		ScheduledTask foliaTask = scheduler.run(plugin, task -> consumer.accept(getTask(taskRef, FoliaTask::new, task)), getOnRetireRunnable(taskRef));
		FoliaTask task = new FoliaTask(foliaTask);
		taskRef.set(task);
		return task;
	}

	@Override
	public @NotNull AgnosticTask runLater(long delay, @NotNull Consumer<AgnosticTask> consumer) {
		AtomicReference<FoliaTask> taskRef = new AtomicReference<>();
		ScheduledTask foliaTask = scheduler.runDelayed(plugin, task -> consumer.accept(getTask(taskRef, FoliaTask::new, task)), getOnRetireRunnable(taskRef), delay);
		FoliaTask task = new FoliaTask(foliaTask);
		taskRef.set(task);
		return task;
	}

	@Override
	public @NotNull AgnosticTask runLater(@NotNull Duration delay, @NotNull Consumer<AgnosticTask> consumer) {
		// convert to ticks
		return runLater(delay.toMillis() / 50, consumer);
	}

	@Override
	public @NotNull AgnosticTask runRepeating(long delay, long period, @NotNull Consumer<AgnosticTask> consumer) {
		AtomicReference<FoliaTask> taskRef = new AtomicReference<>();
		ScheduledTask foliaTask = scheduler.runAtFixedRate(plugin, task -> consumer.accept(getTask(taskRef, FoliaTask::new, task)), getOnRetireRunnable(taskRef), delay, period);
		FoliaTask task = new FoliaTask(foliaTask);
		taskRef.set(task);
		return task;
	}

	@Override
	public @NotNull AgnosticTask runRepeating(@NotNull Duration delay, @NotNull Duration period, @NotNull Consumer<AgnosticTask> consumer) {
		// convert to ticks
		return runRepeating(delay.toMillis() / 50, period.toMillis() / 50, consumer);
	}
}
