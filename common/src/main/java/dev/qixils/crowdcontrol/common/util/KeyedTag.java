package dev.qixils.crowdcontrol.common.util;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
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

public class KeyedTag implements Iterable<Key> {
	protected final Set<Key> keyedSet;

	public KeyedTag() {
		keyedSet = Collections.emptySet();
	}

	public KeyedTag(Key... items) {
		keyedSet = new HashSet<>(items.length);
		keyedSet.addAll(Arrays.asList(items));
	}

	public KeyedTag(Keyed... items) {
		keyedSet = new HashSet<>(items.length);
		for (Keyed item : items)
			keyedSet.add(item.key());
	}

	public KeyedTag(Collection<Keyed> items) {
		this.keyedSet = new HashSet<>(items.size());
		for (Keyed item : items)
			keyedSet.add(item.key());
	}

	public KeyedTag(@NotNull Collection<Key> items1, @Nullable Collection<Key> items2) {
		int itemCount = items1.size();
		if (items2 != null)
			itemCount += items2.size();

		this.keyedSet = new HashSet<>(itemCount);

		keyedSet.addAll(items1);
		if (items2 != null)
			keyedSet.addAll(items2);
	}

	public KeyedTag(KeyedTag tag) {
		this.keyedSet = new HashSet<>(tag.getKeys());
	}

	public Set<Key> getKeys() {
		return Collections.unmodifiableSet(new HashSet<>(keyedSet));
	}

	@NotNull
	@Override
	public Iterator<Key> iterator() {
		return getKeys().iterator();
	}

	@Override
	public Spliterator<Key> spliterator() {
		return getKeys().spliterator();
	}

	public boolean contains(Keyed item) {
		return keyedSet.contains(item.key());
	}

	public boolean contains(Key item) {
		return keyedSet.contains(item);
	}

	public KeyedTag and(Keyed... items) {
		Set<Key> newTag = new HashSet<>(items.length);
		for (Keyed item : items)
			newTag.add(item.key());
		return new KeyedTag(keyedSet, newTag);
	}

	public KeyedTag and(Collection<Keyed> other) {
		return new KeyedTag(keyedSet, other.stream().map(Keyed::key).collect(Collectors.toSet()));
	}

	public KeyedTag and(KeyedTag other) {
		return new KeyedTag(keyedSet, other.getKeys());
	}
}
