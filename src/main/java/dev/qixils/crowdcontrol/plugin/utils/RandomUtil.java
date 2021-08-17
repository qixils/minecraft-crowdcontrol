package dev.qixils.crowdcontrol.plugin.utils;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomUtil {
    private static final Random rand = new Random();

    public static <T> T randomElementFrom(Collection<T> from) {
        // google told me converting to array was bad so i did this idk
        int index = rand.nextInt(from.size());
        int iteration = 0;
        for (T object : from) {
            if (iteration == index) {
                return object;
            }
            ++iteration;
        }
        return null;
    }

    public static <T> T randomElementFrom(List<T> from) {
        return from.get(rand.nextInt(from.size()));
    }

    public static List<Location> randomNearbyBlocks(Location origin, int minRadius, int maxRadius, boolean spawningSpace, Material... materials) {
        List<Location> locations = BlockUtil.getNearbyBlocks(origin, minRadius, maxRadius, spawningSpace, materials);
        if (!locations.isEmpty()) {
            Collections.shuffle(locations, rand);
        }
        return locations;
    }

    public static List<Location> randomNearbyBlocks(Location origin, int maxRadius, boolean spawningSpace, Material... materials) {
        return randomNearbyBlocks(origin, 0, maxRadius, spawningSpace, materials);
    }

    public static Location randomNearbyBlock(Location origin, int minRadius, int maxRadius, boolean spawningSpace, Material... materials) {
        List<Location> locations = BlockUtil.getNearbyBlocks(origin, minRadius, maxRadius, spawningSpace, materials);
        if (!locations.isEmpty()) {
            return (Location) randomElementFrom(locations);
        }
        return null;
    }

    public static Location randomNearbyBlock(Location origin, int maxRadius, boolean spawningSpace, Material... materials) {
        return randomNearbyBlock(origin, 0, maxRadius, spawningSpace, materials);
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
