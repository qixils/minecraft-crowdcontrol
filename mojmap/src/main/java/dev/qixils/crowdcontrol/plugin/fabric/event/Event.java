package dev.qixils.crowdcontrol.plugin.fabric.event;

import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;

public interface Event {

	default void fire() {
		if (!ModdedCrowdControlPlugin.isInstanceAvailable()) return;
		fire(ModdedCrowdControlPlugin.getInstance());
	}

	default void fire(ModdedCrowdControlPlugin plugin) {
		fire(plugin.getEventManager());
	}

	default void fire(EventManager eventManager) {
		eventManager.fire(this);
	}
}
