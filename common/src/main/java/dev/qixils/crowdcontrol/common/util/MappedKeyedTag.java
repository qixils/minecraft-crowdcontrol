package dev.qixils.crowdcontrol.common.util;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Function;

/**
 * A collection of items from a {@link KeyedTag} that have been mapped to a custom type.
 *
 * @param <T> mapped type
 */
public class MappedKeyedTag<T> implements Iterable<T> {
	private final @NotNull Map<Key, T> keyMap;
	private final @NotNull Function<Key, T> mapper;
	private final @NotNull KeyedTag tag;
	private @MonotonicNonNull Set<T> calculatedValues = null;

	/**
	 * Initializes a new mapped keyed tag using a backing tag and a function to map tags to the
	 * desired value.
	 *
	 * @param tag    tag to use as the mapping source
	 * @param mapper function to convert tags to the desired type
	 */
	public MappedKeyedTag(@NotNull KeyedTag tag, @NotNull Function<@NotNull Key, @Nullable T> mapper) {
		this.keyMap = new HashMap<>(tag.getKeys().size());
		this.tag = tag;
		this.mapper = mapper;
	}

	private T map(@NotNull Key key) {
		return keyMap.computeIfAbsent(key, mapper);
	}

	/**
	 * Gets all the keys from the backing tag mapped to the type specified by the mapper function.
	 *
	 * <p>Keys that map to {@code null} will not be included in the resulting collection.</p>
	 *
	 * @return unmodifiable set of unique values
	 */
	@NotNull
	public Set<@NotNull T> getAll() {
		if (calculatedValues != null)
			return calculatedValues;
		tag.getKeys().forEach(this::map);
		return calculatedValues = Collections.unmodifiableSet(new HashSet<>(keyMap.values()));
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
		if (calculatedValues == null) {
			List<Key> keys = new ArrayList<>(tag.getKeys());
			Collections.shuffle(keys, RandomUtil.RNG);
			for (Key key : keys) {
				T item = map(key);
				if (item != null)
					return item;
			}
			throw new IllegalStateException("Could not find a valid mapped value");
		} else if (calculatedValues.isEmpty()) {
			throw new IllegalStateException("Could not find a valid mapped value");
		} else {
			return RandomUtil.randomElementFrom(calculatedValues);
		}
	}

	/**
	 * Returns {@code true} if the provided item is contained in this tag.
	 *
	 * @param item element whose presence in this tag is to be tested
	 * @return {@code true} if this tag contains the provided item
	 */
	@Contract("null -> false; !null -> _")
	public boolean contains(@Nullable T item) {
		return getAll().contains(item);
	}

	/**
	 * Returns {@code true} if the key represented by the provided item is contained in the
	 * underlying tag.
	 *
	 * @param item keyed element whose presence in this tag is to be tested
	 * @return {@code true} if the underlying tag contains the provided item
	 */
	@Contract(pure = true)
	public boolean containsKey(@NotNull Keyed item) {
		return tag.contains(item);
	}

	/**
	 * Returns {@code true} if the provided key is contained in the underlying tag.
	 *
	 * @param item key whose presence in this tag is to be tested
	 * @return {@code true} if the underlying tag contains the provided key
	 */
	@Contract(value = "null -> false; !null -> _", pure = true)
	public boolean containsKey(@Nullable Key item) {
		return tag.contains(item);
	}

}
