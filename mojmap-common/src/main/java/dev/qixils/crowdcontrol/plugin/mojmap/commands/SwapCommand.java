package dev.qixils.crowdcontrol.plugin.mojmap.commands;

import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.plugin.mojmap.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.mojmap.MojmapPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class SwapCommand extends ImmediateCommand {
	private final String effectName = "swap";
	private final String displayName = "Swap Locations";

	public SwapCommand(MojmapPlugin plugin) {
		super(plugin);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		if (players.size() < 2)
			return request.buildResponse().type(ResultType.FAILURE).message("Not enough participating players online");

		// get shuffled list of players
		Collections.shuffle(players, RandomUtil.RNG);
		// create a list offset by one
		List<ServerPlayer> offset = new ArrayList<>(players.size());
		offset.addAll(players.subList(1, players.size()));
		offset.add(players.get(0));
		// get teleport destinations
		Map<ServerPlayer, Location> destinations = new HashMap<>(players.size());
		for (int i = 0; i < players.size(); i++)
			destinations.put(players.get(i), new Location(offset.get(i)));
		// teleport
		sync(() -> destinations.forEach((player, location) -> location.setLocation(player)));
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}

	private record Location(ServerLevel level, double x, double y, double z, float yaw, float pitch) {
		public Location(ServerPlayer player) {
			this(player.getLevel(), player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
		}

		public void setLocation(ServerPlayer player) {
			player.teleportTo(level, x, y, z, yaw, pitch);
		}
	}
}
