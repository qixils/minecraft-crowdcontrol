package dev.qixils.crowdcontrol.common.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Utility class for randomness.
 */
public class RandomUtil {

	/**
	 * A {@link Random} instance.
	 */
	public static final Random RNG = new Random();

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
		if (from.isEmpty())
			throw new IllegalArgumentException("Collection may not be empty");
		int index = RNG.nextInt(from.size());
		int iteration = 0;
		for (T object : from) {
			if (iteration == index) {
				return object;
			}
			++iteration;
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
	public static <T> T randomElementFrom(List<T> from) throws IllegalArgumentException {
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
	public static <T> T randomElementFrom(T[] from) throws IllegalArgumentException {
		if (from.length == 0)
			throw new IllegalArgumentException("Array may not be empty");
		return from[RNG.nextInt(from.length)];
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
	public static <T extends Weighted> T weightedRandom(T[] weightedArray, int totalWeights) throws IllegalArgumentException, NullPointerException {
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
	public static <T extends Weighted> T weightedRandom(T[] weightedArray) throws IllegalArgumentException, NullPointerException {
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


}
