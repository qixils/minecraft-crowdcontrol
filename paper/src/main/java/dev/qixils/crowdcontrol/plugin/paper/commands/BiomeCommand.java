package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.util.TextUtil;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Getter
public class BiomeCommand extends NearbyLocationCommand<Biome> {
	private static final Map<Environment, List<Biome>> BIOMES;

	private static final Map<Environment, List<String>> KEYED_BIOMES = Map.of(
			Environment.NORMAL, List.of(
					// not a conclusive list of biomes
					// rare ones and edge biomes have been omitted
					"OCEAN",
					"PLAINS",
					"DESERT",
					"MOUNTAINS",
					"FOREST",
					"TAIGA",
					"SWAMP",
					"RIVER",
					"FROZEN_OCEAN",
					"FROZEN_RIVER",
					"SNOWY_TUNDRA",
					"SNOWY_MOUNTAINS",
					"MUSHROOM_FIELDS",
					"BEACH",
					"WOODED_HILLS",
					"TAIGA_HILLS",
					"JUNGLE",
					"DEEP_OCEAN",
					"STONE_SHORE",
					"SNOWY_BEACH",
					"SNOWY_PLAINS",
					"BIRCH_FOREST",
					"DARK_FOREST",
					"SNOWY_TAIGA",
					"GIANT_TREE_TAIGA",
					"WOODED_MOUNTAINS",
					"SAVANNA",
					"SAVANNA_PLATEAU",
					"BADLANDS",
					"WOODED_BADLANDS_PLATEAU",
					"BADLANDS_PLATEAU",
					"WARM_OCEAN",
					"LUKEWARM_OCEAN",
					"COLD_OCEAN",
					"DEEP_WARM_OCEAN",
					"DEEP_LUKEWARM_OCEAN",
					"DEEP_COLD_OCEAN",
					"DEEP_FROZEN_OCEAN",
					"SUNFLOWER_PLAINS",
					"DESERT_LAKES",
					"GRAVELLY_MOUNTAINS",
					"FLOWER_FOREST",
					"TAIGA_MOUNTAINS",
					"SWAMP_HILLS",
					"ICE_SPIKES",
					"TALL_BIRCH_FOREST",
					"SNOWY_TAIGA_MOUNTAINS",
					"GIANT_SPRUCE_TAIGA",
					"BAMBOO_JUNGLE",
					"DRIPSTONE_CAVES",
					"LUSH_CAVES",
					"ERODED_BADLANDS",
					// 1.18 biomes (mostly. i think.)
					"FROZEN_PEAKS",
					"GROVE",
					"JAGGED_PEAKS",
					"MEADOW",
					"OLD_GROWTH_BIRCH_FOREST",
					"OLD_GROWTH_PINE_TAIGA",
					"OLD_GROWTH_SPRUCE_TAIGA",
					"SNOWY_SLOPES",
					"SPARSE_JUNGLE",
					"STONY_PEAKS",
					"WINDSWEPT_FOREST",
					"WINDSWEPT_GRAVELLY_HILLS",
					"WINDSWEPT_HILLS",
					"WINDSWEPT_SAVANNA",
					"WOODED_BADLANDS"
			),
			Environment.NETHER, List.of(
					"NETHER_WASTES",
					"SOUL_SAND_VALLEY",
					"CRIMSON_FOREST",
					"WARPED_FOREST",
					"BASALT_DELTAS"
			),
			Environment.THE_END, List.of(
					"THE_END",
					"SMALL_END_ISLANDS",
					"END_MIDLANDS",
					"END_HIGHLANDS",
					"END_BARRENS"
			),
			Environment.CUSTOM, Arrays.stream(Biome.values()).map(Biome::name).toList()
	);

	static {
		Map<Environment, List<Biome>> biomeMap = new HashMap<>(KEYED_BIOMES.size());
		for (Entry<Environment, List<String>> entry : KEYED_BIOMES.entrySet()) {
			List<String> keyedBiomes = entry.getValue();
			List<Biome> biomes = new ArrayList<>(keyedBiomes.size());
			for (String biome : keyedBiomes) {
				try {
					biomes.add(Biome.valueOf(biome));
				} catch (IllegalArgumentException ignored) {
				}
			}
			biomeMap.put(entry.getKey(), biomes);
		}
		BIOMES = biomeMap;
	}

	private final String displayName = "Teleport to a Random Biome";
	private final String effectName = "biome";

	public BiomeCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	protected @Nullable Location search(@NotNull Location origin, @NotNull Biome searchType) {
		return origin.getWorld().locateNearestBiome(origin, searchType, 1000, 30);
	}

	@Override
	protected @NotNull Collection<Biome> getSearchTypes(@NotNull Environment environment) {
		return BIOMES.get(environment);
	}

	@Override
	protected @NotNull String nameOf(@NotNull Biome searchType) {
		return TextUtil.titleCase(searchType.getKey().getKey());
	}

	@Override
	protected @Nullable Biome currentType(@NotNull Location origin) {
		return origin.getBlock().getBiome();
	}
}
