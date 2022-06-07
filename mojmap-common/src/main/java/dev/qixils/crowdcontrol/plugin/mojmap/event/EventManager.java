package dev.qixils.crowdcontrol.plugin.mojmap.event;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Consumer;

public final class EventManager {
	private static final Logger LOGGER = LoggerFactory.getLogger("crowd-control-event-manager");
	private final Multimap<Class<Event>, Consumer<Event>> listeners = HashMultimap.create();

	public <E extends Event> void register(Class<E> eventClass, Consumer<E> listener) {
		listeners.put((Class<Event>) eventClass, (Consumer<Event>) listener);
	}

	public <E extends Event> void unregister(Class<E> eventClass, Consumer<E> listener) {
		listeners.remove(eventClass, listener);
	}

	public void registerListeners(Object object) {
		for (Method method : object.getClass().getMethods()) {
			if (!method.isAnnotationPresent(Listener.class)) continue;
			if (method.getParameterCount() != 1) continue;
			if (!Event.class.isAssignableFrom(method.getParameterTypes()[0])) continue;
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
		Class<? extends Event> eventClass = event.getClass();
		for (Map.Entry<Class<Event>, Consumer<Event>> entry : listeners.entries()) {
			if (!entry.getKey().isAssignableFrom(eventClass)) continue;
			try {
				entry.getValue().accept(event);
			} catch (Throwable t) {
				LOGGER.warn("An error occurred while firing event " + eventClass.getSimpleName(), t);
			}
		}
	}
}
