package dev.qixils.crowdcontrol.plugin.fabric.event;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;

public interface Event {

	default void fire() {
		if (!FabricCrowdControlPlugin.isInstanceAvailable()) return;
		fire(FabricCrowdControlPlugin.getInstance());
	}

	default void fire(FabricCrowdControlPlugin plugin) {
		fire(plugin.getEventManager());
	}

	default void fire(EventManager eventManager) {
		eventManager.fire(this);
	}
}
