package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.plugin.paper.Command;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
public class SwapCommand extends Command implements dev.qixils.crowdcontrol.common.command.ImmediateCommand<Player> {
	private final String effectName = "swap";

	public SwapCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		if (players.size() < 2)
			return request.buildResponse().type(ResultType.FAILURE).message("Not enough participating players online");

		// get shuffled list of players
		Collections.shuffle(players, random);
		// create a list offset by one
		List<Player> offset = new ArrayList<>(players.size());
		offset.addAll(players.subList(1, players.size()));
		offset.add(players.getFirst());
		// get teleport destinations
		Map<Player, Location> destinations = new HashMap<>(players.size());
		for (int i = 0; i < players.size(); ++i)
			destinations.put(players.get(i), offset.get(i).getLocation());
		// teleport
		destinations.forEach((player, location) -> player.getScheduler().run(plugin, $ -> player.teleportAsync(location), null));
		return request.buildResponse().type(Response.ResultType.SUCCESS); // TODO: safer return / folia
	}

	@Override
	public TriState isSelectable() {
		return plugin.getPlayerManager().getAllPlayers().size() <= 1 ? TriState.FALSE : TriState.TRUE;
	}
}
