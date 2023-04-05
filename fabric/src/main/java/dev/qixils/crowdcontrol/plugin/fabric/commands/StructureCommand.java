package dev.qixils.crowdcontrol.plugin.fabric.commands;

import com.mojang.datafixers.util.Pair;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.utils.Location;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureKeys;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.STRUCTURE_SEARCH_RADIUS;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.STRUCTURE_SEARCH_UNEXPLORED;

public class StructureCommand extends NearbyLocationCommand<StructureCommand.StructureGroup> {
	private static final Map<RegistryKey<World>, List<StructureGroup>> STRUCTURES = Map.of(
			World.OVERWORLD, List.of(
					StructureGroup.VILLAGE,
					StructureGroup.PILLAGER_OUTPOST,
					StructureGroup.MINESHAFT,
					StructureGroup.WOODLAND_MANSION,
					StructureGroup.JUNGLE_TEMPLE,
					StructureGroup.DESERT_PYRAMID,
					StructureGroup.IGLOO,
					StructureGroup.SHIPWRECK,
					StructureGroup.SWAMP_HUT,
					StructureGroup.STRONGHOLD,
					StructureGroup.OCEAN_MONUMENT,
					StructureGroup.OCEAN_RUIN,
					StructureGroup.BURIED_TREASURE,
					StructureGroup.RUINED_PORTAL
					// StructureGroup.ANCIENT_CITY | todo: cave teleportation
			),
			World.NETHER, List.of(
					StructureGroup.NETHER_FORTRESS,
					StructureGroup.BASTION_REMNANT,
					StructureGroup.RUINED_PORTAL_NETHER
					// StructureGroup.NETHER_FOSSIL | lame
			),
			World.END, List.of(
					StructureGroup.END_CITY
			)
	);
	@Getter private final String effectName = "structure";

	public StructureCommand(FabricCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	protected @Nullable Location search(@NotNull Location origin, @NotNull StructureGroup searchType) {
		Pair<BlockPos, RegistryEntry<Structure>> pair = origin.level().getChunkManager().getChunkGenerator().locateStructure(origin.level(), searchType.getStructures(origin.level()), origin.pos(), STRUCTURE_SEARCH_RADIUS, STRUCTURE_SEARCH_UNEXPLORED);
		if (pair == null)
			return null;
		return new Location(origin.level(), pair.getFirst());
	}

	@Override
	protected @NotNull Collection<StructureGroup> getSearchTypes(@NotNull ServerWorld level) {
		return STRUCTURES.get(level.getRegistryKey());
	}

	@Override
	protected @NotNull Component nameOf(@NotNull StructureGroup searchType) {
		return searchType.name;
	}

	enum StructureGroup {
		PILLAGER_OUTPOST(StructureKeys.PILLAGER_OUTPOST),
		MINESHAFT(StructureKeys.MINESHAFT, StructureKeys.MINESHAFT_MESA),
		WOODLAND_MANSION(StructureKeys.MANSION),
		JUNGLE_TEMPLE(StructureKeys.JUNGLE_PYRAMID),
		DESERT_PYRAMID(StructureKeys.DESERT_PYRAMID),
		IGLOO(StructureKeys.IGLOO),
		SHIPWRECK(StructureKeys.SHIPWRECK, StructureKeys.SHIPWRECK_BEACHED),
		SWAMP_HUT(StructureKeys.SWAMP_HUT),
		STRONGHOLD(StructureKeys.STRONGHOLD),
		OCEAN_MONUMENT(StructureKeys.MONUMENT),
		OCEAN_RUIN(StructureKeys.OCEAN_RUIN_COLD, StructureKeys.OCEAN_RUIN_WARM),
		NETHER_FORTRESS("Fortress", StructureKeys.FORTRESS),
		NETHER_FOSSIL(StructureKeys.NETHER_FOSSIL),
		END_CITY(StructureKeys.END_CITY),
		BURIED_TREASURE(StructureKeys.BURIED_TREASURE),
		BASTION_REMNANT(StructureKeys.BASTION_REMNANT),
		VILLAGE(StructureKeys.VILLAGE_PLAINS, StructureKeys.VILLAGE_SAVANNA, StructureKeys.VILLAGE_SNOWY, StructureKeys.VILLAGE_TAIGA, StructureKeys.VILLAGE_DESERT),
		RUINED_PORTAL(StructureKeys.RUINED_PORTAL, StructureKeys.RUINED_PORTAL_DESERT, StructureKeys.RUINED_PORTAL_JUNGLE, StructureKeys.RUINED_PORTAL_SWAMP, StructureKeys.RUINED_PORTAL_MOUNTAIN, StructureKeys.RUINED_PORTAL_OCEAN),
		RUINED_PORTAL_NETHER("Ruined Portal", StructureKeys.RUINED_PORTAL_NETHER),
		ANCIENT_CITY(StructureKeys.ANCIENT_CITY),
		;
		private final Component name;
		private final RegistryKey<Structure>[] structures;

		@SafeVarargs
		StructureGroup(String name, RegistryKey<Structure>... structures) {
			this(Component.text(name), structures);
		}

		@SafeVarargs
		StructureGroup(Component name, RegistryKey<Structure>... structures) {
			this.name = name;
			this.structures = structures;
		}

		@SafeVarargs
		StructureGroup(RegistryKey<Structure>... structures) {
			//noinspection deprecation
			this.name = Component.text(WordUtils.capitalizeFully(name().replace('_', ' ')));
			this.structures = structures;
		}

		RegistryEntryList<Structure> getStructures(ServerWorld level) {
			var registry = level.getRegistryManager().getWrapperOrThrow(RegistryKeys.STRUCTURE);
			return RegistryEntryList.of(Arrays.stream(structures).map(registry::getOptional).flatMap(Optional::stream).toList());
		}
	}
}
