package dev.qixils.crowdcontrol.plugin.paper.scheduling;

import dev.qixils.crowdcontrol.common.scheduling.AgnosticExecutor;
import dev.qixils.crowdcontrol.common.scheduling.AgnosticTask;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static dev.qixils.crowdcontrol.plugin.paper.scheduling.FoliaExecutors.getTask;

public record FoliaAsyncExecutor(PaperCrowdControlPlugin plugin) implements AgnosticExecutor {

	@Override
	public @NotNull AgnosticTask run(@NotNull Consumer<AgnosticTask> consumer) {
		AtomicReference<AgnosticTask> taskRef = new AtomicReference<>();
		taskRef.set(new FoliaTask(Bukkit.getAsyncScheduler().runNow(plugin, task -> consumer.accept(getTask(taskRef, task)))));
		return taskRef.get();
	}

	@Override
	public @NotNull AgnosticTask runLater(@NotNull Duration delay, @NotNull Consumer<AgnosticTask> consumer) {
		AtomicReference<AgnosticTask> taskRef = new AtomicReference<>();
		taskRef.set(new FoliaTask(Bukkit.getAsyncScheduler().runDelayed(plugin, task -> consumer.accept(getTask(taskRef, task)), delay.toMillis(), TimeUnit.MILLISECONDS)));
		return taskRef.get();
	}

	@Override
	public @NotNull AgnosticTask runLater(long delay, @NotNull Consumer<AgnosticTask> consumer) {
		// convert ticks to milliseconds
		return runLater(Duration.ofMillis(delay * 50), consumer);
	}

	@Override
	public @NotNull AgnosticTask runRepeating(@NotNull Duration delay, @NotNull Duration period, @NotNull Consumer<AgnosticTask> consumer) {
		AtomicReference<AgnosticTask> taskRef = new AtomicReference<>();
		taskRef.set(new FoliaTask(Bukkit.getAsyncScheduler().runAtFixedRate(plugin, task -> consumer.accept(getTask(taskRef, task)), delay.toMillis(), period.toMillis(), TimeUnit.MILLISECONDS)));
		return taskRef.get();
	}

	@Override
	public @NotNull AgnosticTask runRepeating(long delay, long period, @NotNull Consumer<AgnosticTask> consumer) {
		// convert ticks to milliseconds
		return runRepeating(Duration.ofMillis(delay * 50), Duration.ofMillis(period * 50), consumer);
	}
}
