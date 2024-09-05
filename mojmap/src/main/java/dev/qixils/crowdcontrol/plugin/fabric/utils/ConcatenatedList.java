package dev.qixils.crowdcontrol.plugin.fabric.utils;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class ConcatenatedList<E> extends AbstractList<E> {
	private final @NotNull Iterable<? extends @NotNull List<E>> lists;

	private ConcatenatedList(@NotNull Iterable<? extends @NotNull List<E>> lists) {
		this.lists = lists;
	}

	private static <E> List<List<E>> iterableToList(@NotNull Iterable<List<E>> listsIterable) {
		List<List<E>> listsList;
		if (listsIterable instanceof Collection<List<E>> collection)
			listsList = new ArrayList<>(collection.size());
		else {
			listsList = new ArrayList<>();
			for (List<E> list : listsIterable) {
				listsList.add(list);
			}
		}
		return listsList;
	}

	/**
	 * Creates a new list containing all the elements of the provided lists.
	 * This does not copy the lists, so changes made to the provided lists or the list of lists
	 * will be reflected in the returned list.
	 *
	 * @param lists the lists to concatenate
	 * @return a dynamic immutable list containing all the elements of the provided lists
	 * @param <E> the type of elements in the lists
	 * @see #copyOf(Iterable)
	 */
	@NotNull
	public static <E> List<E> of(@NotNull Iterable<? extends List<E>> lists) {
		return new ConcatenatedList<>(lists);
	}

	/**
	 * Creates a new list containing all the elements of the provided lists at the time of
	 * invocation. Changes made to the provided lists after invocation will not be reflected in the
	 * returned list.
	 *
	 * @param lists the lists to concatenate
	 * @return a fixed immutable list containing all the elements of the provided lists
	 * @param <E> the type of elements in the lists
	 */
	@NotNull
	public static <E> List<E> copyOf(@NotNull Iterable<List<E>> lists) {
		List<E> items = new ArrayList<>();
		for (List<E> list : lists)
			items.addAll(list);
		return Collections.unmodifiableList(items);
	}

	/**
	 * Creates a new list containing all the elements of the provided lists.
	 * This does not copy the lists, so changes made to the provided lists will be reflected in the
	 * returned list. However, changes made to the list of lists will not be reflected.
	 *
	 * @param lists the lists to concatenate
	 * @return a new immutable list containing all the elements of the provided lists
	 * @param <E> the type of elements in the lists
	 */
	@NotNull
	public static <E> List<E> shallowCopyOf(@NotNull Iterable<List<E>> lists) {
		return of(iterableToList(lists));
	}

	private Stream<? extends List<E>> listStream() {
		return StreamSupport.stream(lists.spliterator(), false);
	}

	private List<E> createList() {
		List<E> items = new ArrayList<>(size());
		for (List<E> list : lists)
			items.addAll(list);
		return items;
	}

	@Override
	public int size() {
		return listStream().mapToInt(List::size).sum();
	}

	@Override
	public boolean isEmpty() {
		return listStream().allMatch(List::isEmpty);
	}

	@Override
	public boolean contains(Object o) {
		return listStream().anyMatch(list -> list.contains(o));
	}

	@Override
	public boolean add(E e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("SlowListContainsAll") // this warning is wrong
	@Override
	public boolean containsAll(@NotNull Collection<?> c) {
		return createList().containsAll(c);
	}

	@Override
	public boolean addAll(@NotNull Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(int index, @NotNull Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(@NotNull Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(@NotNull Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public E get(int index) {
		for (List<E> list : lists) {
			if (index < list.size())
				return list.get(index);
			index -= list.size();
		}
		throw new IndexOutOfBoundsException();
	}

	@Override
	public E set(int index, E element) {
		for (List<E> list : lists) {
			if (index < list.size())
				return list.set(index, element);
			index -= list.size();
		}
		throw new IndexOutOfBoundsException();
	}

	@Override
	public E remove(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int indexOf(Object o) {
		int i = 0;
		for (List<E> list : lists) {
			int index = list.indexOf(o);
			if (index != -1)
				return index + i;
			i += list.size();
		}
		return -1;
	}

	@Override
	public int lastIndexOf(Object o) {
		int lastIndex = -1;
		int searchedItems = 0;
		for (List<E> list : lists) {
			lastIndex = Math.max(lastIndex, searchedItems + list.lastIndexOf(o));
			searchedItems += list.size();
		}
		return lastIndex;
	}
}
