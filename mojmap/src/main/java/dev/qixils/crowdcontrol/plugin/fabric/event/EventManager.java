package dev.qixils.crowdcontrol.plugin.fabric.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

@Deprecated
public final class EventManager {
	private static final Logger LOGGER = LoggerFactory.getLogger("CrowdControl/EventManager");
	private final Map<Class<Event>, Collection<Consumer<Event>>> listeners = new ConcurrentHashMap<>();

	public <E extends Event> void register(Class<E> eventClass, Consumer<E> listener) {
		//noinspection unchecked
		listeners.computeIfAbsent((Class<Event>) eventClass, $ -> new ConcurrentLinkedQueue<>()).add((Consumer<Event>) listener);
	}

	public <E extends Event> void unregister(Class<E> eventClass, Consumer<E> listener) {
		Collection<Consumer<Event>> events = listeners.get(eventClass);
		if (events == null) return;
		events.remove(listener);
		if (events.isEmpty()) listeners.remove(eventClass);
	}

	public void registerListeners(Object object) {
		for (Method method : object.getClass().getMethods()) {
			if (!method.isAnnotationPresent(Listener.class)) continue;
			if (method.getParameterCount() != 1) continue;
			if (!Event.class.isAssignableFrom(method.getParameterTypes()[0])) continue;
			//noinspection unchecked
			register((Class<Event>) method.getParameterTypes()[0], event -> {
				try {
					method.invoke(object, event);
				} catch (Throwable t) {
					throw new RuntimeException(t);
				}
			});
		}
	}

	public void fire(Event event) {
		if (listeners.isEmpty()) return;

		Class<? extends Event> eventClass = event.getClass();

		for (Map.Entry<Class<Event>, Collection<Consumer<Event>>> entry : new HashSet<>(listeners.entrySet())) {
			if (!entry.getKey().isAssignableFrom(eventClass)) continue;
			if (entry.getValue().isEmpty()) continue;

			for (Consumer<Event> listener : new ArrayList<>(entry.getValue())) {
				try {
					listener.accept(event);
				} catch (Throwable t) {
					LOGGER.warn("An error occurred while firing event {}", eventClass.getSimpleName(), t);
				}
			}
		}
	}
}
