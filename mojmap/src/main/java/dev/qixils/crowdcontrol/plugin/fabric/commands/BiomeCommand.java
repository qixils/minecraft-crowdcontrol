package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.utils.Location;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.BIOME_SEARCH_RADIUS;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.BIOME_SEARCH_STEP;

@Getter
public class BiomeCommand extends NearbyLocationCommand<Biome> {
	private static final Map<ResourceKey<Level>, List<Biome>> BIOMES = Map.of(
			Level.OVERWORLD, List.of(
					Biomes.PLAINS,
					Biomes.SUNFLOWER_PLAINS,
					Biomes.SNOWY_TUNDRA,
					Biomes.ICE_SPIKES,
					Biomes.DESERT,
					Biomes.SWAMP,
					Biomes.FOREST,
					Biomes.FLOWER_FOREST,
					Biomes.BIRCH_FOREST,
					Biomes.DARK_FOREST,
					Biomes.TAIGA,
					Biomes.SNOWY_TAIGA,
					Biomes.SAVANNA,
					Biomes.SAVANNA_PLATEAU,
					Biomes.MOUNTAINS,
					Biomes.JUNGLE,
					Biomes.BAMBOO_JUNGLE,
					Biomes.BADLANDS,
					Biomes.ERODED_BADLANDS,
					Biomes.RIVER,
					Biomes.FROZEN_RIVER,
					Biomes.BEACH,
					Biomes.SNOWY_BEACH,
					Biomes.WARM_OCEAN,
					Biomes.LUKEWARM_OCEAN,
					Biomes.COLD_OCEAN,
					Biomes.DEEP_LUKEWARM_OCEAN,
					Biomes.DEEP_COLD_OCEAN,
					Biomes.DEEP_FROZEN_OCEAN,
					Biomes.OCEAN,
					Biomes.FROZEN_OCEAN,
					Biomes.DEEP_OCEAN,
					Biomes.MUSHROOM_FIELDS
					// TODO: cave biomes
			),
			Level.NETHER, List.of(
					Biomes.NETHER_WASTES,
					Biomes.SOUL_SAND_VALLEY,
					Biomes.CRIMSON_FOREST,
					Biomes.WARPED_FOREST,
					Biomes.BASALT_DELTAS
			),
			Level.END, List.of(
					Biomes.THE_END,
					Biomes.SMALL_END_ISLANDS,
					Biomes.END_MIDLANDS,
					Biomes.END_HIGHLANDS,
					Biomes.END_BARRENS
			)
	);

	private final String effectName = "biome";

	public BiomeCommand(ModdedCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	protected @Nullable Location search(@NotNull Location origin, @NotNull Biome searchType) {
//		BlockPos pos = origin.pos();
//		var pair = origin.level().getChunkSource().getGenerator().getBiomeSource().findBiomeHorizontal(pos.getX(), pos.getY(), pos.getZ(), BIOME_SEARCH_RADIUS, BIOME_SEARCH_STEP, biome -> biome.is(searchType), origin.level().getRandom(), true, origin.level().getChunkSource().randomState().sampler());
		var pair = origin.level().findNearestBiome(searchType, origin.pos(), BIOME_SEARCH_RADIUS, BIOME_SEARCH_STEP);
		if (pair == null)
			return null;
		return new Location(origin.level(), pair);
	}

	@Override
	protected @NotNull Collection<Biome> getSearchTypes(@NotNull ServerLevel level) {
		return BIOMES.get(level.dimension());
	}

	@Override
	protected @NotNull Component nameOf(@NotNull Biome searchType) {
		return plugin.toAdventure(searchType.getName());
	}

	@Override
	protected @Nullable Biome currentType(@NotNull Location origin) {
		return origin.level().getBiome(origin.pos());
	}
}
