package dev.qixils.crowdcontrol.plugin.fabric.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// TODO: catchup? class validation?

public class EventRegister<T> {
	private static final Logger log = LoggerFactory.getLogger(EventRegister.class);
	private final List<Consumer<T>> listeners = new ArrayList<>();

	public EventRegister() {
	}

	public void fire(T event) {
		for (Consumer<T> listener : listeners) {
			try {
				listener.accept(event);
			} catch (Exception e) {
				log.warn("Consumer {} failed to accept event {}", listener.getClass(), event);
			}
		}
	}

	public void register(Consumer<T> listener) {
		listeners.add(listener);
	}
}
