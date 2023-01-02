package dev.qixils.crowdcontrol.common.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;
import java.util.function.Predicate;

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
	 */
	@NotNull
	public static <T extends Enum<T>> T randomElementFrom(@NotNull Class<T> from) {
		T[] constants = from.getEnumConstants();
		if (constants == null)
			throw new IllegalArgumentException("Class is not an enum");
		return randomElementFrom(constants);
	}

	/**
	 * Picks a valid random item from the provided collection of items.
	 *
	 * <p>If the provided {@code validator} returns {@code false} for a given item, then it will
	 * not be returned by this function. If no valid item can be found then an
	 * {@link Optional#empty() empty optional} is returned.</p>
	 *
	 * <p>Unlike {@link #randomElementFrom(Collection)}, items contained in the provided collection
	 * must not be null.</p>
	 *
	 * @param from      collection of items
	 * @param validator predicate that returns {@code true} if an item is valid
	 * @param <T>       type of item
	 * @return {@link Optional} containing the randomly selected item, or an
	 * {@link Optional#empty() empty optional} if no valid item was found
	 * @throws IllegalArgumentException if the collection is empty
	 */
	@NotNull
	public static <T> Optional<@NotNull T> randomElementFrom(@NotNull Collection<@NotNull T> from,
															 @NotNull Predicate<@NotNull T> validator) {
		if (from.isEmpty())
			throw new IllegalArgumentException("Collection may not be empty");
		List<T> shuffledItems = new ArrayList<>(from);
		Collections.shuffle(shuffledItems, RNG);
		for (T item : from) {
			//noinspection ConstantConditions
			if (item == null)
				throw new IllegalArgumentException("Items in collection must not be null");
			if (validator.test(item)) {
				return Optional.of(item);
			}
		}
		return Optional.empty();
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
	@UnknownNullability
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
	@UnknownNullability
	public static <T extends Weighted> T weightedRandom(T @NotNull [] weightedArray) throws IllegalArgumentException, NullPointerException {
		if (weightedArray.length == 0)
			throw new IllegalArgumentException("Array may not be empty");
		int total = 0;
		for (T t : weightedArray)
			total += t.getWeight();
		return weightedRandom(weightedArray, total);
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
	public static double nextDouble(double from, double to) {
		return from + (RNG.nextDouble() * (to - from));
	}

}
