package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class SwapCommand extends ImmediateCommand {
	private final String effectName = "swap";
	private final String displayName = "Swap Locations";

	public SwapCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		if (players.size() < 2)
			return request.buildResponse().type(ResultType.FAILURE).message("Not enough participating players online");

		// get shuffled list of players
		Collections.shuffle(players, RandomUtil.RNG);
		// create a list offset by one
		List<Player> offset = new ArrayList<>(players.size());
		offset.addAll(players.subList(1, players.size()));
		offset.add(players.get(0));
		// get teleport destinations
		Map<Player, Location<World>> destinations = new HashMap<>(players.size());
		for (int i = 0; i < players.size(); i++)
			destinations.put(players.get(i), offset.get(i).getLocation());
		// teleport
		sync(() -> destinations.forEach(Entity::setLocation));
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
