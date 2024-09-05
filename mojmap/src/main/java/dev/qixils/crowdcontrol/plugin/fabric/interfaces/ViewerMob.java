package dev.qixils.crowdcontrol.plugin.fabric.interfaces;

public interface ViewerMob {

	default boolean cc$isViewerSpawned() {
		return false;
	}

	default void cc$setViewerSpawned() {
		cc$setViewerSpawned(true);
	}

	default void cc$setViewerSpawned(boolean value) {

	}
}
