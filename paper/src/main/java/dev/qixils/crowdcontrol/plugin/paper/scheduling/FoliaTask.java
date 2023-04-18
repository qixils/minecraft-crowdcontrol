package dev.qixils.crowdcontrol.plugin.paper.scheduling;

import dev.qixils.crowdcontrol.common.scheduling.AgnosticTask;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.jetbrains.annotations.Nullable;

public class FoliaTask implements AgnosticTask {
	private final @Nullable ScheduledTask task;
	private boolean retired;

	public FoliaTask(@Nullable ScheduledTask task) {
		this.task = task;
		this.retired = task == null;
	}

	@Override
	public boolean isCancelled() {
		return retired || task == null || task.isCancelled();
	}

	@Override
	public void cancel() {
		if (task != null) task.cancel();
	}

	public void retire() {
		retired = true;
	}
}
