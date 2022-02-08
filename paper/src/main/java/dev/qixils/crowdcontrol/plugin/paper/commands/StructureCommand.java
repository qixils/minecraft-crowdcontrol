package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.util.TextUtil;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.StructureType;
import org.bukkit.World.Environment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
public class StructureCommand extends NearbyLocationCommand<StructureType> {
	private static final Map<Environment, List<StructureType>> STRUCTURES = Map.of(
			Environment.NORMAL, List.of(
					StructureType.MINESHAFT,
					StructureType.VILLAGE,
					StructureType.STRONGHOLD,
					StructureType.JUNGLE_PYRAMID,
					StructureType.OCEAN_RUIN,
					StructureType.DESERT_PYRAMID,
					StructureType.IGLOO,
					StructureType.SWAMP_HUT,
					StructureType.OCEAN_MONUMENT,
					StructureType.WOODLAND_MANSION,
					StructureType.BURIED_TREASURE,
					StructureType.SHIPWRECK,
					StructureType.PILLAGER_OUTPOST,
					StructureType.RUINED_PORTAL
			),
			Environment.NETHER, List.of(
					StructureType.NETHER_FORTRESS,
					StructureType.RUINED_PORTAL,
					StructureType.BASTION_REMNANT
			),
			Environment.THE_END, List.of(
					StructureType.END_CITY
			),
			Environment.CUSTOM, List.copyOf(StructureType.getStructureTypes().values())
	);
	private final String displayName = "Teleport to a Nearby Structure";
	private final String effectName = "structure";

	public StructureCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	protected @Nullable Location search(@NotNull Location origin, @NotNull StructureType searchType) {
		return origin.getWorld().locateNearestStructure(origin, searchType, 100, false);
	}

	@Override
	protected @NotNull Collection<StructureType> getSearchTypes(@NotNull Environment environment) {
		return STRUCTURES.get(environment);
	}

	@Override
	protected @NotNull String nameOf(@NotNull StructureType searchType) {
		return TextUtil.titleCase(searchType.getName());
	}
}
