package dev.qixils.crowdcontrol.common.util;

import java.util.Collection;
import java.util.List;
import java.util.Random;

public class RandomUtil {
    public static final Random RNG = new Random();

    public static <T> T randomElementFrom(Collection<T> from) {
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

    public static <T> T randomElementFrom(List<T> from) {
        if (from.isEmpty())
            throw new IllegalArgumentException("List may not be empty");
        return from.get(RNG.nextInt(from.size()));
    }

    public static <T> T randomElementFrom(T[] from) {
        if (from.length == 0)
            throw new IllegalArgumentException("Array may not be empty");
        return from[RNG.nextInt(from.length)];
    }

    public static <T extends Weighted> T weightedRandom(T[] weightedArray, int totalWeights) {
        // Weighted random code based off of https://stackoverflow.com/a/6737362
        int idx = 0;
        for (double r = Math.random() * totalWeights; idx < weightedArray.length - 1; ++idx) {
            r -= weightedArray[idx].getWeight();
            if (r <= 0.0) break;
        }
        return weightedArray[idx];
    }


}
