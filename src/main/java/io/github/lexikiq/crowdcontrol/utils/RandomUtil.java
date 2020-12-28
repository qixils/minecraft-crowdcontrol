package io.github.lexikiq.crowdcontrol.utils;

import java.util.Collection;
import java.util.List;
import java.util.Random;

public class RandomUtil {
    private static final Random rand = new Random();
    public static Object randomElementFrom(Collection<?> from) {
        // google told me converting to array was bad so i did this idk
        int index = rand.nextInt(from.size());
        int iteration = 0;
        for (Object object : from) {
            if (iteration == index) {
                return object;
            }
            ++iteration;
        }
        return null;
    }
    public static Object randomElementFrom(List<?> from) {
        return from.get(rand.nextInt(from.size()));
    }
}
