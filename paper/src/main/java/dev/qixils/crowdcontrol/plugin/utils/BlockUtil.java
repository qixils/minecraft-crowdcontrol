package dev.qixils.crowdcontrol.plugin.utils;

import dev.qixils.crowdcontrol.common.util.CommonTags;
import dev.qixils.crowdcontrol.common.util.RandomUtil;
import lombok.Builder;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class BlockUtil {
    public static final MaterialTag FLOWERS = new MaterialTag(CommonTags.FLOWERS);
    public static final MaterialTag STONES_TAG = new MaterialTag(CommonTags.STONES);
    public static final MaterialTag TORCHES = new MaterialTag(CommonTags.TORCHES);

    public static Predicate<Location> SPAWNING_SPACE = location -> location.getBlock().isPassable()
            && location.clone().add(0, 1, 0).getBlock().isPassable()
            && location.clone().subtract(0, 1, 0).getBlock().isSolid();

    public static BlockFinder.BlockFinderBuilder blockFinderBuilder() {
        return BlockFinder.builder();
    }

    @Builder(builderClassName = "BlockFinderBuilder")
    public static class BlockFinder {
        private static final Predicate<Location> TRUE = $ -> true; // reduce object creation ?? idk

        private final World origin;
        private final List<Vector> locations;
        @Builder.Default
        private final Predicate<Location> locationValidator = TRUE;

        @Nullable
        public Location next() {
            if (locations.isEmpty())
                return null;
            Location location = locations.remove(0).toLocation(origin);
            if (locationValidator.test(location))
                return location;
            return next();
        }

        @NotNull
        public List<Location> getAll() {
            List<Location> list = new ArrayList<>();
            Location next = next();
            while (next != null) {
                list.add(next);
                next = next();
            }
            return list;
        }

        public static class BlockFinderBuilder {
            private Vector originPos = null;
            private Integer maxRadius = null;
            private int minRadius = 0;
            private boolean shuffleLocations = true;

            public BlockFinderBuilder maxRadius(int maxRadius) {
                this.maxRadius = maxRadius;
                return this;
            }

            public BlockFinderBuilder minRadius(int minRadius) {
                this.minRadius = minRadius;
                return this;
            }

            public BlockFinderBuilder shuffleLocations(boolean shuffleLocations) {
                this.shuffleLocations = shuffleLocations;
                return this;
            }

            public BlockFinderBuilder originPos(Vector originPos) {
                this.originPos = originPos;
                return this;
            }

            public BlockFinderBuilder origin(World origin) {
                this.origin = origin;
                return this;
            }

            public BlockFinderBuilder origin(Location origin) {
                return originPos(origin.toVector()).origin(origin.getWorld());
            }

            public BlockFinder build() {
                if (maxRadius == null)
                    throw new IllegalStateException("maxRadius is not set");
                if (origin == null)
                    throw new IllegalStateException("origin is not set");

                if (this.locations == null)
                    // pre-define capacity
                    this.locations = new ArrayList<>((int) (Math.pow(maxRadius, 3) - Math.pow(minRadius, 3)));

                int origX = originPos.getBlockX();
                int origY = originPos.getBlockY();
                int origZ = originPos.getBlockZ();
                for (int x = -maxRadius; x <= maxRadius; x++) {
                    if (Math.abs(x) < minRadius)
                        continue;
                    for (int y = -maxRadius; y <= maxRadius; y++) {
                        if (Math.abs(y) < minRadius)
                            continue;
                        for (int z = -maxRadius; z <= maxRadius; z++) {
                            if (Math.abs(z) < minRadius)
                                continue;
                            locations.add(new Vector(origX + x, origY + y, origZ + z));
                        }
                    }
                }

                if (this.shuffleLocations)
                    Collections.shuffle(this.locations, RandomUtil.RNG);

                return new BlockFinder(origin, locations, locationValidator$value);
            }
        }
    }
}
