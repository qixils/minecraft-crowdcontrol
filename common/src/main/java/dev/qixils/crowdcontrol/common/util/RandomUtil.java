package dev.qixils.crowdcontrol.common.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Utility class for randomness.
 */
public class RandomUtil {

	/**
	 * A {@link Random} instance.
	 */
	public static final @NotNull Random RNG = new Random();

	/**
	 * Picks a random item from the provided collection of items.
	 * The returned value is non-null assuming the collection consists of non-null items.
	 *
	 * @param from collection of items
	 * @param <T>  type of item
	 * @return randomly selected item
	 * @throws IllegalArgumentException if the collection is empty
	 */
	@UnknownNullability
	public static <T> T randomElementFrom(@NotNull Collection<@Nullable T> from) throws IllegalArgumentException {
		if (from instanceof List<?>)
			return randomElementFrom((List<T>) from);
		if (from.isEmpty())
			throw new IllegalArgumentException("Collection may not be empty");
		int index = RNG.nextInt(from.size());
		int iteration = 0;
		for (T object : from) {
			if (iteration++ == index)
				return object;
		}
		throw new IllegalStateException("Could not find a random object. Was the collection updated?");
	}

	/**
	 * Picks a random item from the provided list of items.
	 * The returned value is non-null assuming the list consists of non-null items.
	 *
	 * @param from list of items
	 * @param <T>  type of item
	 * @return randomly selected item
	 * @throws IllegalArgumentException if the list is empty
	 */
	@UnknownNullability
	public static <T> T randomElementFrom(@NotNull List<@Nullable T> from) throws IllegalArgumentException {
		if (from.isEmpty())
			throw new IllegalArgumentException("List may not be empty");
		return from.get(RNG.nextInt(from.size()));
	}

	/**
	 * Picks a random item from the provided array of items.
	 * The returned value is non-null assuming the array consists of non-null items.
	 *
	 * @param from array of items
	 * @param <T>  type of item
	 * @return randomly selected item
	 * @throws IllegalArgumentException if the array is empty
	 */
	@UnknownNullability
	public static <T> T randomElementFrom(T @NotNull [] from) throws IllegalArgumentException {
		if (from.length == 0)
			throw new IllegalArgumentException("Array may not be empty");
		return from[RNG.nextInt(from.length)];
	}

	/**
	 * Picks a random item from the provided enum.
	 * The returned value is non-null.
	 *
	 * @param from enum class
	 * @param <T>  type of enum
	 * @return randomly selected enum constant
	 * @throws IllegalArgumentException if the class is not an enum
	 * @deprecated may not work well with dynamically-injected `values` methods from ex-enums; directly calling `values` is preferred
	 */
	@NotNull
	@Deprecated
	public static <T extends Enum<T>> T randomElementFrom(@NotNull Class<T> from) {
		T[] constants = from.getEnumConstants();
		if (constants == null) {
			try {
				//noinspection unchecked
				constants = (T[]) Arrays.stream(from.getMethods())
					.filter(method -> "values".equals(method.getName()) && method.getReturnType().isArray() && Modifier.isStatic(method.getModifiers()))
					.findAny()
					.orElseThrow(() -> new IllegalArgumentException("Class is not an enum"))
					.invoke(null);
			} catch (Exception e) {
				throw new IllegalArgumentException("Class is not an enum");
			}
		}
		return randomElementFrom(constants);
	}

	/**
	 * Picks a random item from the provided iterator.
	 * The returned value is non-null assuming the iterator consists of non-null items.
	 *
	 * @param from iterator of items
	 * @param <T>  type of item
	 * @return randomly selected item
	 * @throws IllegalArgumentException if the iterator is empty
	 */
	@UnknownNullability
	public static <T> T randomElementFrom(@NotNull Iterator<@Nullable T> from) throws IllegalArgumentException {
		List<T> list = new ArrayList<>();
		while (from.hasNext())
			list.add(from.next());
		return randomElementFrom(list);
	}

	/**
	 * Picks a random item from the provided iterable.
	 * The returned value is non-null assuming the iterable consists of non-null items.
	 *
	 * @param from iterable of items
	 * @param <T>  type of item
	 * @return randomly selected item
	 * @throws IllegalArgumentException if the iterable is empty
	 */
	@UnknownNullability
	public static <T> T randomElementFrom(@NotNull Iterable<@Nullable T> from) throws IllegalArgumentException {
		return randomElementFrom(from.iterator());
	}

	/**
	 * Picks a random item from the provided stream.
	 * The returned value is non-null assuming the stream consists of non-null items.
	 *
	 * @param from stream of items
	 * @param <T>  type of item
	 * @return randomly selected item
	 * @throws IllegalArgumentException if the stream is empty
	 */
	@UnknownNullability
	public static <T> T randomElementFrom(@NotNull Stream<@Nullable T> from) throws IllegalArgumentException {
		return randomElementFrom(from.iterator());
	}

	/**
	 * Picks a valid random item from the provided iterator of items.
	 * <p>
	 * If the provided {@code validator} returns {@code false} for a given item, then it will
	 * not be returned by this function. If no valid item can be found then an
	 * {@link Optional#empty() empty optional} is returned.</p>
	 * </p>
	 * Null items will be ignored and an empty iterator will produce an empty optional.
	 *
	 * @param from      iterator of items
	 * @param validator predicate that returns {@code true} if an item is valid
	 * @param <T>       type of item
	 * @return {@link Optional} containing the randomly selected item, or an
	 * {@link Optional#empty() empty optional} if no valid item was found
	 */
	@NotNull
	public static <T> Optional<@NotNull T> randomElementFrom(@NotNull Iterator<@Nullable T> from,
															 @NotNull Predicate<@NotNull T> validator) {
		List<T> items = new ArrayList<>();
		while (from.hasNext()) {
			T item = from.next();
			if (item != null && validator.test(item))
				items.add(item);
		}
		if (items.isEmpty())
			return Optional.empty();
		return Optional.of(randomElementFrom(items));
	}

	/**
	 * Picks a valid random item from the provided iterable of items.
	 * <p>
	 * If the provided {@code validator} returns {@code false} for a given item, then it will
	 * not be returned by this function. If no valid item can be found then an
	 * {@link Optional#empty() empty optional} is returned.</p>
	 * </p>
	 * Null items will be ignored and an empty iterable will produce an empty optional.
	 *
	 * @param from      iterable of items
	 * @param validator predicate that returns {@code true} if an item is valid
	 * @param <T>       type of item
	 * @return {@link Optional} containing the randomly selected item, or an
	 * {@link Optional#empty() empty optional} if no valid item was found
	 */
	@NotNull
	public static <T> Optional<@NotNull T> randomElementFrom(@NotNull Iterable<@Nullable T> from,
															 @NotNull Predicate<@NotNull T> validator) {
		return randomElementFrom(from.iterator(), validator);
	}

	/**
	 * Picks a valid random item from the provided collection of items.
	 * <p>
	 * If the provided {@code validator} returns {@code false} for a given item, then it will
	 * not be returned by this function. If no valid item can be found then an
	 * {@link Optional#empty() empty optional} is returned.</p>
	 * </p>
	 * Null items will be ignored and an empty collection will produce an empty optional.
	 *
	 * @param from      collection of items
	 * @param validator predicate that returns {@code true} if an item is valid
	 * @param <T>       type of item
	 * @return {@link Optional} containing the randomly selected item, or an
	 * {@link Optional#empty() empty optional} if no valid item was found
	 */
	@NotNull
	public static <T> Optional<@NotNull T> randomElementFrom(@NotNull Collection<@Nullable T> from,
															 @NotNull Predicate<@NotNull T> validator) {
		List<T> items = new ArrayList<>(from);
		items.removeIf(item -> item == null || !validator.test(item));
		if (items.isEmpty())
			return Optional.empty();
		return Optional.of(randomElementFrom(items));
	}

	/**
	 * Picks a valid random item from the provided stream of items.
	 * <p>
	 * If the provided {@code validator} returns {@code false} for a given item, then it will
	 * not be returned by this function. If no valid item can be found then an
	 * {@link Optional#empty() empty optional} is returned.</p>
	 * </p>
	 * Null items will be ignored and an empty stream will produce an empty optional.
	 *
	 * @param from      stream of items
	 * @param validator predicate that returns {@code true} if an item is valid
	 * @param <T>       type of item
	 * @return {@link Optional} containing the randomly selected item, or an
	 * {@link Optional#empty() empty optional} if no valid item was found
	 */
	@NotNull
	public static <T> Optional<@NotNull T> randomElementFrom(@NotNull Stream<@Nullable T> from,
															 @NotNull Predicate<@NotNull T> validator) {
		return randomElementFrom(from.iterator(), validator);
	}

	/**
	 * Picks a valid random item from the provided array of items.
	 * <p>
	 * If the provided {@code validator} returns {@code false} for a given item, then it will
	 * not be returned by this function. If no valid item can be found then an
	 * {@link Optional#empty() empty optional} is returned.</p>
	 * </p>
	 * Null items will be ignored and an empty array will produce an empty optional.
	 *
	 * @param from      array of items
	 * @param validator predicate that returns {@code true} if an item is valid
	 * @param <T>       type of item
	 * @return {@link Optional} containing the randomly selected item, or an
	 * {@link Optional#empty() empty optional} if no valid item was found
	 */
	@NotNull
	public static <T> Optional<@NotNull T> randomElementFrom(T @NotNull [] from,
															 @NotNull Predicate<@NotNull T> validator) {
		List<T> items = new ArrayList<>();
		for (T item : from) {
			if (item != null && validator.test(item))
				items.add(item);
		}
		if (items.isEmpty())
			return Optional.empty();
		return Optional.of(randomElementFrom(items));
	}

	/**
	 * Picks a random item from an array of weighted items.
	 *
	 * @param weightedArray array of weighted items
	 * @param totalWeights  total number of weights
	 * @param <T>           type of item
	 * @return randomly selected item
	 * @throws IllegalArgumentException if the array is empty
	 * @throws NullPointerException     if an item in the array is null
	 */
	@NotNull
	public static <T extends Weighted> T weightedRandom(T @NotNull [] weightedArray, int totalWeights) throws IllegalArgumentException, NullPointerException {
		if (weightedArray.length == 0)
			throw new IllegalArgumentException("Array may not be empty");
		// Weighted random code based off of https://stackoverflow.com/a/6737362
		int idx = 0;
		for (double r = Math.random() * totalWeights; idx < weightedArray.length - 1; ++idx) {
			r -= weightedArray[idx].getWeight();
			if (r <= 0.0) break;
		}
		return weightedArray[idx];
	}

	/**
	 * Picks a random item from an array of weighted items.
	 *
	 * @param weightedArray array of weighted items
	 * @param <T>           type of item
	 * @return randomly selected item
	 * @throws IllegalArgumentException if the array is empty
	 * @throws NullPointerException     if an item in the array is null
	 */
	@NotNull
	public static <T extends Weighted> T weightedRandom(T @NotNull [] weightedArray) throws IllegalArgumentException, NullPointerException {
		if (weightedArray.length == 0)
			throw new IllegalArgumentException("Array may not be empty");
		int total = 0;
		for (T t : weightedArray)
			total += t.getWeight();
		return weightedRandom(weightedArray, total);
	}

	/**
	 * Picks a random item from a map of weighted items.
	 *
	 * @param weightedMap  map of weighted items
	 * @param totalWeights total number of weights
	 * @param <T> 		   type of item
	 * @return randomly selected item
	 * @throws IllegalArgumentException if the map is empty
	 * @throws NullPointerException     if an item in the map is null
	 */
	@NotNull
	public static <T> T weightedRandom(@NotNull Map<@NotNull T, @NotNull Integer> weightedMap, int totalWeights) throws IllegalArgumentException, NullPointerException {
		if (weightedMap.isEmpty())
			throw new IllegalArgumentException("Map may not be empty");
		// Weighted random code based off of https://stackoverflow.com/a/6737362
		List<Map.Entry<T, Integer>> entries = new ArrayList<>(weightedMap.entrySet());
		int idx = 0;
		for (double r = Math.random() * totalWeights; idx < entries.size() - 1; ++idx) {
			r -= entries.get(idx).getValue();
			if (r <= 0.0) break;
		}
		return entries.get(idx).getKey();
	}

	/**
	 * Picks a random item from a map of weighted items.
	 *
	 * @param weightedMap map of weighted items
	 * @param <T> 		  type of item
	 * @return randomly selected item
	 * @throws IllegalArgumentException if the map is empty
	 * @throws NullPointerException     if an item in the map is null
	 */
	@NotNull
	public static <T> T weightedRandom(@NotNull Map<@NotNull T, @NotNull Integer> weightedMap) throws IllegalArgumentException, NullPointerException {
		if (weightedMap.isEmpty())
			throw new IllegalArgumentException("Map may not be empty");
		int total = 0;
		for (int i : weightedMap.values())
			total += i;
		return weightedRandom(weightedMap, total);
	}

	/**
	 * Randomly generates a new integer between the specified inclusive bounds.
	 *
	 * @param from inclusive beginning bound
	 * @param to   inclusive ending bound
	 * @return random integer between [{@code from},{@code to}]
	 */
	public static int nextInclusiveInt(int from, int to) {
		return from + RNG.nextInt(to - from + 1);
	}

	/**
	 * Randomly generates a new double between {@code from} (inclusive) and {@code to} (exclusive).
	 *
	 * @param from inclusive minimum value
	 * @param to   exclusive maximum value
	 * @return random double between {@code from} and {@code to}
	 */
	@Deprecated
	public static double nextDouble(double from, double to) {
		return RNG.nextDouble(from, to);
		//return from + (RNG.nextDouble() * (to - from));
	}

}
