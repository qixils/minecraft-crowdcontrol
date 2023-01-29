package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.utils.Location;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.STRUCTURE_SEARCH_RADIUS;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.STRUCTURE_SEARCH_UNEXPLORED;

public class StructureCommand extends NearbyLocationCommand<StructureCommand.StructureGroup> {
	private static final Map<ResourceKey<Level>, List<StructureGroup>> STRUCTURES = Map.of(
			Level.OVERWORLD, List.of(
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
			Level.NETHER, List.of(
					StructureGroup.NETHER_FORTRESS,
					StructureGroup.BASTION_REMNANT,
					StructureGroup.RUINED_PORTAL_NETHER
					// StructureGroup.NETHER_FOSSIL | lame
			),
			Level.END, List.of(
					StructureGroup.END_CITY
			)
	);
	@Getter private final String effectName = "structure";

	public StructureCommand(FabricCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	protected @Nullable Location search(@NotNull Location origin, @NotNull StructureGroup searchType) {
		var pair = origin.level().getChunkSource().getGenerator().findNearestMapStructure(origin.level(), searchType.getStructures(origin.level()), origin.pos(), STRUCTURE_SEARCH_RADIUS, STRUCTURE_SEARCH_UNEXPLORED);
		if (pair == null)
			return null;
		return new Location(origin.level(), pair.getFirst());
	}

	@Override
	protected @NotNull Collection<StructureGroup> getSearchTypes(@NotNull ServerLevel level) {
		return STRUCTURES.get(level.dimension());
	}

	@Override
	protected @NotNull Component nameOf(@NotNull StructureGroup searchType) {
		return searchType.name;
	}

	enum StructureGroup {
		PILLAGER_OUTPOST(BuiltinStructures.PILLAGER_OUTPOST),
		MINESHAFT(BuiltinStructures.MINESHAFT, BuiltinStructures.MINESHAFT_MESA),
		WOODLAND_MANSION(BuiltinStructures.WOODLAND_MANSION),
		JUNGLE_TEMPLE(BuiltinStructures.JUNGLE_TEMPLE),
		DESERT_PYRAMID(BuiltinStructures.DESERT_PYRAMID),
		IGLOO(BuiltinStructures.IGLOO),
		SHIPWRECK(BuiltinStructures.SHIPWRECK, BuiltinStructures.SHIPWRECK_BEACHED),
		SWAMP_HUT(BuiltinStructures.SWAMP_HUT),
		STRONGHOLD(BuiltinStructures.STRONGHOLD),
		OCEAN_MONUMENT(BuiltinStructures.OCEAN_MONUMENT),
		OCEAN_RUIN(BuiltinStructures.OCEAN_RUIN_COLD, BuiltinStructures.OCEAN_RUIN_WARM),
		NETHER_FORTRESS("Fortress", BuiltinStructures.FORTRESS),
		NETHER_FOSSIL(BuiltinStructures.NETHER_FOSSIL),
		END_CITY(BuiltinStructures.END_CITY),
		BURIED_TREASURE(BuiltinStructures.BURIED_TREASURE),
		BASTION_REMNANT(BuiltinStructures.BASTION_REMNANT),
		VILLAGE(BuiltinStructures.VILLAGE_PLAINS, BuiltinStructures.VILLAGE_SAVANNA, BuiltinStructures.VILLAGE_SNOWY, BuiltinStructures.VILLAGE_TAIGA, BuiltinStructures.VILLAGE_DESERT),
		RUINED_PORTAL(BuiltinStructures.RUINED_PORTAL_STANDARD, BuiltinStructures.RUINED_PORTAL_DESERT, BuiltinStructures.RUINED_PORTAL_JUNGLE, BuiltinStructures.RUINED_PORTAL_SWAMP, BuiltinStructures.RUINED_PORTAL_MOUNTAIN, BuiltinStructures.RUINED_PORTAL_OCEAN),
		RUINED_PORTAL_NETHER("Ruined Portal", BuiltinStructures.RUINED_PORTAL_NETHER),
		ANCIENT_CITY(BuiltinStructures.ANCIENT_CITY),
		;
		private final Component name;
		private final ResourceKey<Structure>[] structures;

		@SafeVarargs
		StructureGroup(String name, ResourceKey<Structure>... structures) {
			this(Component.text(name), structures);
		}

		@SafeVarargs
		StructureGroup(Component name, ResourceKey<Structure>... structures) {
			this.name = name;
			this.structures = structures;
		}

		@SafeVarargs
		StructureGroup(ResourceKey<Structure>... structures) {
			//noinspection deprecation
			this.name = Component.text(WordUtils.capitalizeFully(name().replace('_', ' ')));
			this.structures = structures;
		}

		HolderSet<Structure> getStructures(ServerLevel level) {
			var registry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
			return HolderSet.direct(Arrays.stream(structures).map(registry::get).flatMap(Optional::stream).toList());
		}
	}
}
