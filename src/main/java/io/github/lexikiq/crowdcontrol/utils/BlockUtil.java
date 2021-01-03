package io.github.lexikiq.crowdcontrol.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class BlockUtil {
    public static final Material[] AIR_ARRAY = new Material[]{Material.AIR, Material.CAVE_AIR, Material.VOID_AIR};
    public static final Set<Material> AIR_BLOCKS = ImmutableSet.copyOf(AIR_ARRAY);
    public static final List<Material> FLOWERS = ImmutableList.of(
            Material.POPPY,
            Material.DANDELION,
            Material.BLUE_ORCHID,
            Material.ALLIUM,
            Material.AZURE_BLUET,
            Material.ORANGE_TULIP,
            Material.RED_TULIP,
            Material.PINK_TULIP,
            Material.WHITE_TULIP,
            Material.OXEYE_DAISY,
            Material.CORNFLOWER,
            Material.LILY_OF_THE_VALLEY,
            Material.WITHER_ROSE
    );
    public static final Material[] STONES = new Material[]{
            Material.ANDESITE,
            Material.DIORITE,
            Material.STONE,
            Material.DIRT,
            Material.GRAVEL,
            Material.GRANITE,
            Material.NETHERRACK,
            Material.GRASS_BLOCK,
            Material.SAND,
            Material.RED_SAND,
            Material.SANDSTONE,
            Material.TERRACOTTA,
            Material.RED_SANDSTONE,
            Material.ORANGE_TERRACOTTA,
            Material.BLACK_TERRACOTTA,
            Material.BLUE_TERRACOTTA,
            Material.BROWN_TERRACOTTA,
            Material.CYAN_TERRACOTTA,
            Material.GRAY_TERRACOTTA,
            Material.GREEN_TERRACOTTA,
            Material.LIGHT_BLUE_TERRACOTTA,
            Material.LIGHT_GRAY_TERRACOTTA,
            Material.LIME_TERRACOTTA,
            Material.MAGENTA_TERRACOTTA,
            Material.PINK_TERRACOTTA,
            Material.PURPLE_TERRACOTTA,
            Material.RED_TERRACOTTA,
            Material.WHITE_TERRACOTTA,
            Material.YELLOW_TERRACOTTA,
            Material.GRASS,
            Material.GRASS_PATH,
            Material.TALL_GRASS,
            Material.POPPY,
            Material.DANDELION,
            Material.BLUE_ORCHID,
            Material.ALLIUM,
            Material.AZURE_BLUET,
            Material.ORANGE_TULIP,
            Material.RED_TULIP,
            Material.PINK_TULIP,
            Material.WHITE_TULIP,
            Material.OXEYE_DAISY,
            Material.CORNFLOWER,
            Material.LILY_OF_THE_VALLEY,
            Material.WITHER_ROSE
    };
    public static final Set<Material> STONES_SET = ImmutableSet.copyOf(STONES);
    public static final Material[] TORCH_ARRAY = new Material[]{
            Material.TORCH,
            Material.REDSTONE_TORCH,
            Material.SOUL_TORCH,
            Material.REDSTONE_WALL_TORCH,
            Material.WALL_TORCH,
            Material.SOUL_WALL_TORCH
    };
    public static final Set<Material> TORCH_SET = ImmutableSet.copyOf(TORCH_ARRAY);
    public static final Set<Material> MATERIAL_SET = ImmutableSet.copyOf(Material.values());

    // these should honestly probably return a list (set?) of blocks but i'm too lazy to fix all the instances
    public static List<Location> getNearbyBlocks(Location origin, int minRadius, int maxRadius, boolean spawningSpace, Material... materials) {
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
        return locations;
    }

    public static List<Location> getNearbyBlocks(Location origin, int maxRadius, boolean spawningSpace, Material... materials) {
        return getNearbyBlocks(origin, 0, maxRadius, spawningSpace, materials);
    }
}
