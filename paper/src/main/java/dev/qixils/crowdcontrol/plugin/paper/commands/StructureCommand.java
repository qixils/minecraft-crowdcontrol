package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.VoidCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.StructureType;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
public class StructureCommand extends VoidCommand {
	private static final Map<Environment, List<StructureType>> STRUCTURES = Map.of(
			Environment.NORMAL, List.of(
					// TODO
			),
			Environment.NETHER, List.of(
					// TODO
			),
			Environment.THE_END, List.of(
					// TODO
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
			if (!world.canGenerateStructures())
				continue;
			Location location = player.getLocation();
			List<StructureType> structures = STRUCTURES.get(world.getEnvironment());
			Collections.shuffle(structures, random);
			for (StructureType structure : structures) {
				Location destination = world.locateNearestStructure(location, structure, 70, false);
				if (destination == null)
					continue;
				player.teleportAsync(destination);
				break;
			}
		}
	}
}
