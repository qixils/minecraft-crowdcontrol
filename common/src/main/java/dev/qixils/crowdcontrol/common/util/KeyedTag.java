package dev.qixils.crowdcontrol.common.util;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Collectors;

/**
 * A collection of {@link Key}s.
 */
public final class KeyedTag implements Iterable<Key> {

	/**
	 * The collection of {@link Key}s.
	 */
	private final Set<Key> keyedSet;

	/**
	 * Initializes an empty tag for chaining.
	 */
	public KeyedTag() {
		keyedSet = Collections.emptySet();
	}

	/**
	 * Initializes a tag using a variable amount of {@link Key}s.
	 *
	 * @param items adventure keys
	 */
	public KeyedTag(Key @NotNull ... items) {
		keyedSet = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(items)));
	}

	/**
	 * Initializes a tag using a variable amount of {@link Keyed} items.
	 *
	 * @param items keyed items
	 */
	public KeyedTag(Keyed @NotNull ... items) {
		Set<Key> keyedSet = new HashSet<>(items.length);
		for (Keyed item : items)
			keyedSet.add(item.key());
		this.keyedSet = Collections.unmodifiableSet(keyedSet);
	}

	/**
	 * Initializes a tag using a collection of {@link Keyed} items.
	 *
	 * @param items keyed items
	 */
	public KeyedTag(@NotNull Collection<@NotNull Keyed> items) {
		Set<Key> keyedSet = new HashSet<>(items.size());
		for (Keyed item : items)
			keyedSet.add(item.key());
		this.keyedSet = Collections.unmodifiableSet(keyedSet);
	}

	/**
	 * Initializes a tag using collections of {@link Key}s.
	 *
	 * @param items1 a collection of items
	 * @param items2 another collection of items (or null)
	 */
	public KeyedTag(@NotNull Collection<@NotNull Key> items1, @Nullable Collection<@NotNull Key> items2) {
		int itemCount = items1.size();
		if (items2 != null)
			itemCount += items2.size();

		Set<Key> keyedSet = new HashSet<>(itemCount);

		keyedSet.addAll(items1);
		if (items2 != null)
			keyedSet.addAll(items2);

		this.keyedSet = Collections.unmodifiableSet(keyedSet);
	}

	/**
	 * Initializes a copy of another {@link KeyedTag}.
	 *
	 * @param tag tag to copy
	 */
	public KeyedTag(KeyedTag tag) {
		this.keyedSet = Collections.unmodifiableSet(new HashSet<>(tag.getKeys()));
	}

	/**
	 * Gets the keys stored in this tag.
	 *
	 * @return unique unmodifiable set of keys
	 */
	@NotNull
	public Set<Key> getKeys() {
		return keyedSet;
	}

	@NotNull
	@Override
	public Iterator<Key> iterator() {
		return getKeys().iterator();
	}

	@NotNull
	@Override
	public Spliterator<Key> spliterator() {
		return getKeys().spliterator();
	}

	/**
	 * Returns {@code true} if the key represented by the provided item is contained in this tag.
	 *
	 * @param item keyed element whose presence in this tag is to be tested
	 * @return {@code true} if this tag contains the provided key
	 */
	public boolean contains(@NotNull Keyed item) {
		return keyedSet.contains(item.key());
	}

	/**
	 * Returns {@code true} if the provided key is contained in this tag.
	 *
	 * @param item key whose presence in this tag is to be tested
	 * @return {@code true} if this tag contains the provided key
	 */
	@Contract("null -> false; !null -> _")
	public boolean contains(@Nullable Key item) {
		return keyedSet.contains(item);
	}

	/**
	 * Creates a new {@link KeyedTag} containing both the contents of this tag and the provided
	 * keys.
	 *
	 * @param items new keys to introduce
	 * @return new {@link KeyedTag}
	 */
	@NotNull
	@Contract(value = "_ -> new", pure = true)
	public KeyedTag and(Key @NotNull ... items) {
		return new KeyedTag(keyedSet, Arrays.asList(items));
	}

	/**
	 * Creates a new {@link KeyedTag} containing both the contents of this tag and the keys from the
	 * provided items.
	 *
	 * @param items new keyed items to introduce
	 * @return new {@link KeyedTag}
	 */
	@NotNull
	@Contract(value = "_ -> new", pure = true)
	public KeyedTag and(Keyed @NotNull ... items) {
		Set<Key> newTag = new HashSet<>(items.length);
		for (Keyed item : items)
			newTag.add(item.key());
		return new KeyedTag(keyedSet, newTag);
	}

	/**
	 * Creates a new {@link KeyedTag} containing both the contents of this tag and the keys from the
	 * provided items.
	 *
	 * @param other new keyed items to introduce
	 * @return new {@link KeyedTag}
	 */
	@NotNull
	@Contract(value = "_ -> new", pure = true)
	public KeyedTag and(@NotNull Collection<@NotNull Keyed> other) {
		return new KeyedTag(keyedSet, other.stream().map(Keyed::key).collect(Collectors.toSet()));
	}

	/**
	 * Creates a new {@link KeyedTag} containing the contents of both this tag and the provided tag.
	 *
	 * @param other {@link KeyedTag} to merge with
	 * @return new {@link KeyedTag}
	 */
	@NotNull
	@Contract(value = "_ -> new", pure = true)
	public KeyedTag and(@NotNull KeyedTag other) {
		return new KeyedTag(keyedSet, other.getKeys());
	}

	/**
	 * Creates a new {@link KeyedTag} containing the contents of this tag except tags that are equal
	 * to one of the provided keys.
	 *
	 * @param keys array of keys to exclude from the new tag
	 * @return new {@link KeyedTag}
	 */
	@NotNull
	@Contract(value = "_ -> new", pure = true)
	public KeyedTag except(Key @NotNull ... keys) {
		return except(Arrays.asList(keys));
	}

	/**
	 * Creates a new {@link KeyedTag} containing the contents of this tag except tags that are equal
	 * to one of the provided keys.
	 *
	 * @param keys collection of keys to exclude from the new tag
	 * @return new {@link KeyedTag}
	 */
	@NotNull
	@Contract(value = "_ -> new", pure = true)
	public KeyedTag except(@NotNull Collection<Key> keys) {
		Set<Key> newSet = new HashSet<>(keyedSet.size());
		for (Key key : keyedSet) {
			if (!keys.contains(key)) {
				newSet.add(key);
			}
		}
		return new KeyedTag(newSet, null);
	}
}
