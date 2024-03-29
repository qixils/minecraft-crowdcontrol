package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.util.TextUtil;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.StructureType;
import org.bukkit.World.Environment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.STRUCTURE_SEARCH_RADIUS;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.STRUCTURE_SEARCH_UNEXPLORED;

@Getter
public class StructureCommand extends NearbyLocationCommand<StructureType> {
	private static final Map<Environment, List<StructureType>> STRUCTURES;
	private static final Map<Environment, List<String>> KEYED_STRUCTURES = Map.of(
			Environment.NORMAL, List.of(
					"mineshaft",
					"village",
					"stronghold",
					"jungle_pyramid",
					"ocean_ruin",
					"desert_pyramid",
					"igloo",
					"swamp_hut",
					"monument",
					"mansion",
					"buried_treasure",
					"shipwreck",
					"pillager_outpost",
					"ruined_portal"
					// todo cave teleportation (deep dark)
			),
			Environment.NETHER, List.of(
					"fortress",
					"ruined_portal",
					"bastion_remnant"
			),
			Environment.THE_END, List.of(
					"endcity"
			),
			Environment.CUSTOM,
			StructureType.getStructureTypes().values().stream().map(StructureType::getName).toList()
	);
	private final String effectName = "structure";

	static {
		Map<Environment, List<StructureType>> biomeMap = new HashMap<>(KEYED_STRUCTURES.size());
		for (Entry<Environment, List<String>> entry : KEYED_STRUCTURES.entrySet()) {
			List<String> keyedBiomes = entry.getValue();
			List<StructureType> biomes = new ArrayList<>(keyedBiomes.size());
			Map<String, StructureType> structures = StructureType.getStructureTypes();
			for (String biome : keyedBiomes) {
				StructureType structure = structures.get(biome);
				if (structure != null) {
					biomes.add(structure);
				}
			}
			biomeMap.put(entry.getKey(), biomes);
		}
		STRUCTURES = biomeMap;
	}

	public StructureCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	protected @Nullable Location search(@NotNull Location origin, @NotNull StructureType searchType) {
		return origin.getWorld().locateNearestStructure(origin, searchType, STRUCTURE_SEARCH_RADIUS, STRUCTURE_SEARCH_UNEXPLORED);
	}

	@Override
	protected @NotNull Collection<StructureType> getSearchTypes(@NotNull Environment environment) {
		return STRUCTURES.get(environment);
	}

	@Override
	protected @NotNull Component nameOf(@NotNull StructureType searchType) {
		return Component.text(TextUtil.titleCase(searchType.getName())); // TODO: i18n (need to manually translate)
	}
}
