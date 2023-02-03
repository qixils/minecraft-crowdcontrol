package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.fabric.utils.Location;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.DIG_RADIUS;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.getDigDepth;

@Getter
public class DigCommand extends ImmediateCommand {
	private final String effectName = "dig";

	public DigCommand(FabricCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Set<Location> locations = new HashSet<>();
		int depth = getDigDepth();
		for (ServerPlayer player : players) {
			Location playerLocation = new Location(player);
			for (double x = -DIG_RADIUS; x <= DIG_RADIUS; ++x) {
				for (int y = depth; y < 0; ++y) {
					for (double z = -DIG_RADIUS; z <= DIG_RADIUS; ++z) {
						Location block = playerLocation.add(x, y, z);
						if (!block.block().isAir())
							locations.add(block);
					}
				}
			}
		}

		if (locations.isEmpty())
			return request.buildResponse().type(Response.ResultType.RETRY).message("Streamer(s) not standing on any blocks");

		sync(() -> {
			for (Location location : locations)
				location.block(Blocks.AIR.defaultBlockState());
		});

		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
