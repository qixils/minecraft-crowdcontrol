package dev.qixils.crowdcontrol.plugin.paper.scheduling;

import dev.qixils.crowdcontrol.common.scheduling.AgnosticTask;
import org.bukkit.scheduler.BukkitTask;

public record PaperTask(BukkitTask task) implements AgnosticTask {
	@Override
	public boolean isCancelled() {
		return task.isCancelled();
	}

	@Override
	public void cancel() {
		task.cancel();
	}
}
