package dev.qixils.crowdcontrol.common.scheduling;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Future;

public class FutureTask implements AgnosticTask {
	private Future<?> future;

	public FutureTask() {
	}

	public FutureTask(Future<?> future) {
		this.future = future;
	}

	@ApiStatus.Internal
	@NotNull
	FutureTask setFuture(@NotNull Future<?> future) {
		this.future = future;
		return this;
	}

	@Override
	public boolean isCancelled() {
		return future.isCancelled();
	}

	@Override
	public void cancel() {
		future.cancel(false);
	}
}
