package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.util.TextUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.utils.Location;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.STRUCTURE_SEARCH_RADIUS;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.STRUCTURE_SEARCH_UNEXPLORED;

public class StructureCommand extends NearbyLocationCommand<StructureFeature<?>> {
	private static final Map<ResourceKey<Level>, List<StructureFeature<?>>> STRUCTURES = Map.of(
			Level.OVERWORLD, List.of(
					StructureFeature.VILLAGE,
					StructureFeature.PILLAGER_OUTPOST,
					StructureFeature.MINESHAFT,
					StructureFeature.WOODLAND_MANSION,
					StructureFeature.JUNGLE_TEMPLE,
					StructureFeature.DESERT_PYRAMID,
					StructureFeature.IGLOO,
					StructureFeature.SHIPWRECK,
					StructureFeature.SWAMP_HUT,
					StructureFeature.STRONGHOLD,
					StructureFeature.OCEAN_MONUMENT,
					StructureFeature.OCEAN_RUIN,
					StructureFeature.BURIED_TREASURE,
					StructureFeature.RUINED_PORTAL
			),
			Level.NETHER, List.of(
					StructureFeature.NETHER_BRIDGE,
					StructureFeature.BASTION_REMNANT,
					StructureFeature.RUINED_PORTAL
					// StructureGroup.NETHER_FOSSIL | lame
			),
			Level.END, List.of(
					StructureFeature.END_CITY
			)
	);
	@Getter private final String effectName = "structure";

	public StructureCommand(ModdedCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	protected @Nullable Location search(@NotNull Location origin, @NotNull StructureFeature<?> searchType) {
		var pair = origin.level().findNearestMapFeature(searchType, origin.pos(), STRUCTURE_SEARCH_RADIUS, STRUCTURE_SEARCH_UNEXPLORED);
		if (pair == null)
			return null;
		return new Location(origin.level(), pair);
	}

	@Override
	protected @NotNull Collection<StructureFeature<?>> getSearchTypes(@NotNull ServerLevel level) {
		return STRUCTURES.get(level.dimension());
	}

	@Override
	protected @NotNull Component nameOf(@NotNull StructureFeature<?> searchType) {
		return Component.text(TextUtil.titleCase(searchType.getFeatureName()));
	}
}
