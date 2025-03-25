package dev.qixils.crowdcontrol.plugin.fabric.event;

public interface CancellableEvent extends Event {
	boolean cancelled();
	void cancelled(boolean cancelled);
	default void cancel() {
		cancelled(true);
	}
}
