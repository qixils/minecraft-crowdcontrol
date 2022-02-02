package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.util.TextBuilder;
import dev.qixils.crowdcontrol.common.util.TextUtil;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.VoidCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.StructureType;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
public class StructureCommand extends VoidCommand {
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
	public void voidExecute(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		request.buildResponse().type(ResultType.SUCCESS).send();

		for (Player player : players) {
			World world = player.getWorld();
			plugin.getLogger().info("ajkfnd jskm  cf");
			if (!world.canGenerateStructures())
				continue;
			plugin.getLogger().info("fdlks,.mfv ");
			Location location = player.getLocation();
			List<StructureType> structures = new ArrayList<>(STRUCTURES.get(world.getEnvironment()));
			Collections.shuffle(structures, random);
			sync(() -> {
				for (StructureType structure : structures) {
					plugin.getLogger().info("hmmmmmm ");
					Location destination = world.locateNearestStructure(location, structure, 70, false);
					if (destination == null)
						continue;
					plugin.getLogger().info("!!!!!!!!!!!!! ");
					destination = destination.toHighestLocation().add(0, 1, 0);
					player.teleportAsync(destination).thenAccept(success -> {
						if (!success)
							return;
						announce(player, request);
						player.sendActionBar(new TextBuilder(
								"You have been teleported to the nearest ",
								NamedTextColor.WHITE
						).next(
								TextUtil.titleCase(structure.getName()),
								NamedTextColor.YELLOW
						));
					});
					break;
				}
			});
		}
	}
}
