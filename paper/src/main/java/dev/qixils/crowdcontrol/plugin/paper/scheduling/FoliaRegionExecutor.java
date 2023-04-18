package dev.qixils.crowdcontrol.plugin.paper.scheduling;

import dev.qixils.crowdcontrol.common.scheduling.AgnosticExecutor;
import dev.qixils.crowdcontrol.common.scheduling.AgnosticTask;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static dev.qixils.crowdcontrol.plugin.paper.scheduling.FoliaExecutors.getTask;

public record FoliaRegionExecutor(PaperCrowdControlPlugin plugin, World world, int chunkX, int chunkZ) implements AgnosticExecutor {

	public FoliaRegionExecutor(PaperCrowdControlPlugin plugin, Location location) {
		this(plugin, location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4);
	}

	@Override
	public @NotNull AgnosticTask run(@NotNull Consumer<AgnosticTask> consumer) {
		AtomicReference<AgnosticTask> taskRef = new AtomicReference<>();
		taskRef.set(new FoliaTask(Bukkit.getRegionScheduler().run(plugin, world, chunkX, chunkZ, task -> consumer.accept(getTask(taskRef, task)))));
		return taskRef.get();
	}

	@Override
	public @NotNull AgnosticTask runLater(long delay, @NotNull Consumer<AgnosticTask> consumer) {
		AtomicReference<AgnosticTask> taskRef = new AtomicReference<>();
		taskRef.set(new FoliaTask(Bukkit.getRegionScheduler().runDelayed(plugin, world, chunkX, chunkZ, task -> consumer.accept(getTask(taskRef, task)), delay)));
		return taskRef.get();
	}

	@Override
	public @NotNull AgnosticTask runLater(@NotNull Duration delay, @NotNull Consumer<AgnosticTask> consumer) {
		// convert to ticks
		return runLater(delay.toMillis() / 50, consumer);
	}

	@Override
	public @NotNull AgnosticTask runRepeating(long delay, long period, @NotNull Consumer<AgnosticTask> consumer) {
		AtomicReference<AgnosticTask> taskRef = new AtomicReference<>();
		taskRef.set(new FoliaTask(Bukkit.getRegionScheduler().runAtFixedRate(plugin, world, chunkX, chunkZ, task -> consumer.accept(getTask(taskRef, task)), delay, period)));
		return taskRef.get();
	}

	@Override
	public @NotNull AgnosticTask runRepeating(@NotNull Duration delay, @NotNull Duration period, @NotNull Consumer<AgnosticTask> consumer) {
		// convert to ticks
		return runRepeating(delay.toMillis() / 50, period.toMillis() / 50, consumer);
	}
}
