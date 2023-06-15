package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.fabric.utils.Location;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
public class SwapCommand extends ImmediateCommand {
	private final String effectName = "swap";

	public SwapCommand(FabricCrowdControlPlugin plugin) {
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
		sync(() -> destinations.forEach((player, location) -> location.teleportHere(player)));
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}

	@Override
	public TriState isSelectable() {
		return plugin.getPlayerManager().getAllPlayers().size() <= 1 ? TriState.FALSE : TriState.TRUE;
	}
}
