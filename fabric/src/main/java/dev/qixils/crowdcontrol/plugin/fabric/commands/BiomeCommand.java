package dev.qixils.crowdcontrol.plugin.fabric.commands;

import com.mojang.datafixers.util.Pair;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.utils.Location;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.BIOME_SEARCH_RADIUS;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.BIOME_SEARCH_STEP;

@Getter
public class BiomeCommand extends NearbyLocationCommand<RegistryKey<Biome>> {
	private static final Map<RegistryKey<World>, List<RegistryKey<Biome>>> BIOMES = Map.of(
			World.OVERWORLD, List.of(
					BiomeKeys.PLAINS,
					BiomeKeys.SUNFLOWER_PLAINS,
					BiomeKeys.SNOWY_PLAINS,
					BiomeKeys.ICE_SPIKES,
					BiomeKeys.DESERT,
					BiomeKeys.SWAMP,
					BiomeKeys.MANGROVE_SWAMP,
					BiomeKeys.FOREST,
					BiomeKeys.FLOWER_FOREST,
					BiomeKeys.BIRCH_FOREST,
					BiomeKeys.DARK_FOREST,
					BiomeKeys.TAIGA,
					BiomeKeys.SNOWY_TAIGA,
					BiomeKeys.SAVANNA,
					BiomeKeys.SAVANNA_PLATEAU,
					BiomeKeys.WINDSWEPT_HILLS,
					BiomeKeys.WINDSWEPT_GRAVELLY_HILLS,
					BiomeKeys.WINDSWEPT_FOREST,
					BiomeKeys.WINDSWEPT_SAVANNA,
					BiomeKeys.JUNGLE,
					BiomeKeys.SPARSE_JUNGLE,
					BiomeKeys.BAMBOO_JUNGLE,
					BiomeKeys.BADLANDS,
					BiomeKeys.ERODED_BADLANDS,
					BiomeKeys.WOODED_BADLANDS,
					BiomeKeys.MEADOW,
					BiomeKeys.GROVE,
					BiomeKeys.SNOWY_SLOPES,
					BiomeKeys.FROZEN_PEAKS,
					BiomeKeys.JAGGED_PEAKS,
					BiomeKeys.STONY_PEAKS,
					BiomeKeys.RIVER,
					BiomeKeys.FROZEN_RIVER,
					BiomeKeys.BEACH,
					BiomeKeys.SNOWY_BEACH,
					BiomeKeys.STONY_SHORE,
					BiomeKeys.WARM_OCEAN,
					BiomeKeys.LUKEWARM_OCEAN,
					BiomeKeys.COLD_OCEAN,
					BiomeKeys.DEEP_LUKEWARM_OCEAN,
					BiomeKeys.DEEP_COLD_OCEAN,
					BiomeKeys.DEEP_FROZEN_OCEAN,
					BiomeKeys.OCEAN,
					BiomeKeys.FROZEN_OCEAN,
					BiomeKeys.DEEP_OCEAN,
					BiomeKeys.MUSHROOM_FIELDS
					// TODO: cave biomes
			),
			World.NETHER, List.of(
					BiomeKeys.NETHER_WASTES,
					BiomeKeys.SOUL_SAND_VALLEY,
					BiomeKeys.CRIMSON_FOREST,
					BiomeKeys.WARPED_FOREST,
					BiomeKeys.BASALT_DELTAS
			),
			World.END, List.of(
					BiomeKeys.THE_END,
					BiomeKeys.SMALL_END_ISLANDS,
					BiomeKeys.END_MIDLANDS,
					BiomeKeys.END_HIGHLANDS,
					BiomeKeys.END_BARRENS
			)
	);

	private final String effectName = "biome";

	public BiomeCommand(FabricCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	protected @Nullable Location search(@NotNull Location origin, @NotNull RegistryKey<Biome> searchType) {
//		BlockPos pos = origin.pos();
//		var pair = origin.level().getChunkSource().getGenerator().getBiomeSource().findBiomeHorizontal(pos.getX(), pos.getY(), pos.getZ(), BIOME_SEARCH_RADIUS, BIOME_SEARCH_STEP, biome -> biome.is(searchType), origin.level().getRandom(), true, origin.level().getChunkSource().randomState().sampler());
		Pair<BlockPos, RegistryEntry<Biome>> pair = origin.level().locateBiome(biome -> biome.matchesKey(searchType), origin.pos(), BIOME_SEARCH_RADIUS, BIOME_SEARCH_STEP, BIOME_SEARCH_STEP);
		if (pair == null)
			return null;
		return new Location(origin.level(), pair.getFirst());
	}

	@Override
	protected @NotNull Collection<RegistryKey<Biome>> getSearchTypes(@NotNull ServerWorld level) {
		return BIOMES.get(level.getRegistryKey());
	}

	@Override
	protected @NotNull Component nameOf(@NotNull RegistryKey<Biome> searchType) {
		Identifier id = searchType.getValue();
		return Component.translatable("biome." + id.getNamespace() + "." + id.getPath());
	}

	@Override
	protected @Nullable RegistryKey<Biome> currentType(@NotNull Location origin) {
		return origin.level().getBiome(origin.pos()).getKey().orElse(null);
	}
}
