package dev.qixils.crowdcontrol.common.scheduling;

/**
 * A task that can be cancelled.
 */
public interface AgnosticTask {

	/**
	 * Returns whether this task has been cancelled.
	 *
	 * @return whether this task has been cancelled
	 */
	boolean isCancelled();

	/**
	 * Attempts to cancel this task.
	 */
	void cancel();
}
