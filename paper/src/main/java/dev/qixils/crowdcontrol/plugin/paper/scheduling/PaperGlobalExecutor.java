package dev.qixils.crowdcontrol.plugin.paper.scheduling;

import dev.qixils.crowdcontrol.common.scheduling.AgnosticExecutor;
import dev.qixils.crowdcontrol.common.scheduling.AgnosticTask;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R3.scheduler.CraftScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static dev.qixils.crowdcontrol.plugin.paper.scheduling.FoliaExecutors.getTask;
import static org.bukkit.craftbukkit.v1_19_R3.scheduler.CraftTask.NO_REPEATING;

public record PaperGlobalExecutor(PaperCrowdControlPlugin plugin) implements AgnosticExecutor {

	@Override
	public @NotNull AgnosticTask run(@NotNull Consumer<AgnosticTask> consumer) {
		return runRepeating(0, NO_REPEATING, consumer);
	}

	@Override
	public @NotNull AgnosticTask runLater(long delay, @NotNull Consumer<AgnosticTask> consumer) {
		return runRepeating(delay, NO_REPEATING, consumer);
	}

	@Override
	public @NotNull AgnosticTask runLater(@NotNull Duration delay, @NotNull Consumer<AgnosticTask> consumer) {
		// convert to ticks
		return runLater(delay.toMillis() / 50, consumer);
	}

	@Override
	public @NotNull AgnosticTask runRepeating(long delay, long period, @NotNull Consumer<AgnosticTask> consumer) {
		CraftScheduler scheduler = (CraftScheduler) Bukkit.getScheduler();
		AtomicReference<AgnosticTask> taskRef = new AtomicReference<>();
		Consumer<BukkitTask> bukkitConsumer = task -> consumer.accept(getTask(taskRef, task));
		AgnosticTask task = new PaperTask(scheduler.runTaskTimer(plugin, (Object) bukkitConsumer, delay, period));
		taskRef.set(task);
		return task;
	}

	@Override
	public @NotNull AgnosticTask runRepeating(@NotNull Duration delay, @NotNull Duration period, @NotNull Consumer<AgnosticTask> consumer) {
		return runRepeating(delay.toMillis() / 50, period.toMillis() / 50, consumer);
	}
}
