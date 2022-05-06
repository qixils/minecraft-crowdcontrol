package dev.qixils.crowdcontrol.plugin.mojmap.event;

import dev.qixils.crowdcontrol.plugin.mojmap.MojmapPlugin;

public interface Event {
	default void fire(MojmapPlugin<?> plugin) {
		fire(plugin.getEventManager());
	}

	default void fire(EventManager eventManager) {
		eventManager.fire(this);
	}
}
