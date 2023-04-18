package dev.qixils.crowdcontrol.common.scheduling;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * An executor that runs tasks on a pool of threads.
 */
public class AsyncExecutor implements AgnosticExecutor {
	private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

	@Override
	public @NotNull AgnosticTask run(@NotNull Consumer<AgnosticTask> consumer) {
		FutureTask task = new FutureTask();
		return task.setFuture(executor.submit(() -> {
			if (!task.isCancelled()) // fallback
				consumer.accept(task);
		}));
	}

	@Override
	public @NotNull AgnosticTask runLater(long delay, @NotNull Consumer<AgnosticTask> consumer) {
		return runLater(Duration.ofMillis(delay * 50), consumer);
	}

	@Override
	public @NotNull AgnosticTask runLater(@NotNull Duration delay, @NotNull Consumer<AgnosticTask> consumer) {
		FutureTask task = new FutureTask();
		return task.setFuture(executor.schedule(() -> {
			if (!task.isCancelled()) // fallback
				consumer.accept(task);
		}, delay.toMillis(), TimeUnit.MILLISECONDS));
	}

	@Override
	public @NotNull AgnosticTask runRepeating(long delay, long period, @NotNull Consumer<AgnosticTask> consumer) {
		return runRepeating(Duration.ofMillis(delay * 50), Duration.ofMillis(period * 50), consumer);
	}

	@Override
	@NotNull
	public AgnosticTask runRepeating(@NotNull Duration delay, @NotNull Duration period, @NotNull Consumer<AgnosticTask> consumer) {
		FutureTask task = new FutureTask();
		return task.setFuture(executor.scheduleAtFixedRate(() -> {
			if (!task.isCancelled()) // fallback
				consumer.accept(task);
		}, delay.toMillis(), period.toMillis(), TimeUnit.MILLISECONDS));
	}
}
