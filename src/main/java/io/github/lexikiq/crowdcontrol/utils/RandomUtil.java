package io.github.lexikiq.crowdcontrol.utils;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.*;

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

    public static Location randomNearbyBlock(Location origin, int minRadius, int maxRadius, boolean spawningSpace, Material... materials) {
        List<Location> locations = new ArrayList<>();
        // fun 3D iteration
        for (int x = -maxRadius; x <= maxRadius; x++) {
            if (Math.abs(x) < minRadius) {continue;}
            for (int y = (spawningSpace ? -1 : -maxRadius); y <= maxRadius; y++) {
                if (Math.abs(y) < minRadius && !spawningSpace) {continue;}
                for (int z = -maxRadius; z <= maxRadius; z++) {
                    if (Math.abs(z) < minRadius) {continue;}

                    // actual block checking code
                    Location base = origin.clone().add(x, y, z);
                    boolean toAdd = Arrays.stream(materials).anyMatch((m) -> m == base.getBlock().getType());

                    if (toAdd && spawningSpace) {
                        // basic spawning space checking (idk if it's possible to get the mob bounding boxes for proper stuff, but idrc)
                        Material above = base.clone().add(0, 1, 0).getBlock().getType();
                        Material below = base.clone().add(0, -1, 0).getBlock().getType();
                        toAdd = Arrays.stream(materials).anyMatch((m) -> m == above) && Arrays.stream(materials).noneMatch((m) -> m == below);
                    }

                    if (toAdd) {
                        locations.add(base);
                    }
                }
            }
        }
        if (locations.size() > 0) {
            return (Location) randomElementFrom(locations);
        }
        return null;
    }

    public static Location randomNearbyBlock(Location origin, int maxRadius, boolean spawningSpace, Material... materials) {
        return randomNearbyBlock(origin, 0, maxRadius, spawningSpace, materials);
    }
}
