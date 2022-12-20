package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.utils.Location;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.data.worldgen.Structures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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
		var pair = origin.level().getChunkSource().getGenerator().findNearestMapStructure(origin.level(), searchType.structures, origin.pos(), STRUCTURE_SEARCH_RADIUS, STRUCTURE_SEARCH_UNEXPLORED);
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
		PILLAGER_OUTPOST(Structures.PILLAGER_OUTPOST),
		MINESHAFT(Structures.MINESHAFT, Structures.MINESHAFT_MESA),
		WOODLAND_MANSION(Structures.WOODLAND_MANSION),
		JUNGLE_TEMPLE(Structures.JUNGLE_TEMPLE),
		DESERT_PYRAMID(Structures.DESERT_PYRAMID),
		IGLOO(Structures.IGLOO),
		SHIPWRECK(Structures.SHIPWRECK, Structures.SHIPWRECK_BEACHED),
		SWAMP_HUT(Structures.SWAMP_HUT),
		STRONGHOLD(Structures.STRONGHOLD),
		OCEAN_MONUMENT(Structures.OCEAN_MONUMENT),
		OCEAN_RUIN(Structures.OCEAN_RUIN_COLD, Structures.OCEAN_RUIN_WARM),
		NETHER_FORTRESS("Fortress", Structures.FORTRESS),
		NETHER_FOSSIL(Structures.NETHER_FOSSIL),
		END_CITY(Structures.END_CITY),
		BURIED_TREASURE(Structures.BURIED_TREASURE),
		BASTION_REMNANT(Structures.BASTION_REMNANT),
		VILLAGE(Structures.VILLAGE_PLAINS, Structures.VILLAGE_SAVANNA, Structures.VILLAGE_SNOWY, Structures.VILLAGE_TAIGA, Structures.VILLAGE_DESERT),
		RUINED_PORTAL(Structures.RUINED_PORTAL_STANDARD, Structures.RUINED_PORTAL_DESERT, Structures.RUINED_PORTAL_JUNGLE, Structures.RUINED_PORTAL_SWAMP, Structures.RUINED_PORTAL_MOUNTAIN, Structures.RUINED_PORTAL_OCEAN),
		RUINED_PORTAL_NETHER("Ruined Portal", Structures.RUINED_PORTAL_NETHER),
		ANCIENT_CITY(Structures.ANCIENT_CITY),
		;
		private final Component name;
		private final HolderSet<Structure> structures;

		@SafeVarargs
		StructureGroup(String name, Holder<Structure>... structures) {
			this(Component.text(name), structures);
		}

		@SafeVarargs
		StructureGroup(Component name, Holder<Structure>... structures) {
			this.name = name;
			this.structures = HolderSet.direct(structures);
		}

		@SafeVarargs
		StructureGroup(Holder<Structure>... structures) {
			this.name = Component.text(WordUtils.capitalizeFully(name().replace('_', ' ')));
			this.structures = HolderSet.direct(structures);
		}
	}
}
