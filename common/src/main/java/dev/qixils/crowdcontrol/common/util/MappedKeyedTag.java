package dev.qixils.crowdcontrol.common.util;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Function;

public class MappedKeyedTag<T> implements Iterable<T> {
	private final Map<Key, T> keyMap = new HashMap<>();
	private final Function<Key, T> mapper;
	private final KeyedTag tag;
	private Collection<T> calculatedValues = null;

	public MappedKeyedTag(@NotNull KeyedTag tag, @NotNull Function<@NotNull Key, @Nullable T> mapper) {
		this.tag = tag;
		this.mapper = mapper;
	}

	private T map(@NotNull Key key) {
		return keyMap.computeIfAbsent(key, mapper);
	}

	/**
	 * Gets the unmodifiable collection of values contained within this tag.
	 *
	 * @return unmodifiable collection of values
	 */
	public Collection<T> getAll() {
		if (calculatedValues != null)
			return calculatedValues;
		tag.getKeys().forEach(this::map);
		return calculatedValues = keyMap.values();
	}

	@NotNull
	@Override
	public Iterator<T> iterator() {
		return getAll().iterator();
	}

	@Override
	public Spliterator<T> spliterator() {
		return getAll().spliterator();
	}

	/**
	 * Gets a random value from this tag.
	 *
	 * @return random value
	 * @throws IllegalStateException if all associated keys map to a null value
	 */
	@NotNull
	public T getRandom() throws IllegalStateException {
		List<Key> keys = new ArrayList<>(tag.getKeys());
		Collections.shuffle(keys, RandomUtil.RNG);
		for (Key key : keys) {
			T item = map(key);
			if (item != null)
				return item;
		}
		throw new IllegalStateException("Could not find a valid mapped value");
	}

	/**
	 * Determines if the given object is contained within this tag.
	 *
	 * @param object object to check
	 * @return true if the object is contained within this tag
	 */
	@SuppressWarnings("SuspiciousMethodCalls")
	public boolean contains(Object object) {
		return getAll().contains(object);
	}

}
