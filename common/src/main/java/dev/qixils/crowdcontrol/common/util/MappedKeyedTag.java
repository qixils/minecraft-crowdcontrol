package dev.qixils.crowdcontrol.common.util;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Function;

public class MappedKeyedTag<T extends Keyed> implements Iterable<T> {
	private final Map<Key, T> keyMap = new HashMap<>();
	private final Function<Key, T> mapper;
	private final KeyedTag tag;
	private Collection<T> calculatedValues = null;

	public MappedKeyedTag(KeyedTag tag, Function<Key, T> mapper) {
		this.tag = tag;
		this.mapper = mapper;
	}

	private T map(Key key) {
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
	 */
	public T getRandom() {
		return map(RandomUtil.randomElementFrom(tag.getKeys()));
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
