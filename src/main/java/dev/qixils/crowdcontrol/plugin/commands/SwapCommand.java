package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
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

    public SwapCommand(CrowdControlPlugin plugin) {
        super(plugin);
    }

    @Override
    public Response.@NotNull Builder executeImmediately(@NotNull Request request) {
        List<Player> players = CrowdControlPlugin.getPlayers();
        if (players.size() < 2)
            return request.buildResponse().type(Response.ResultType.UNAVAILABLE).message("Not enough players online");

        // get shuffled list of players
        Collections.shuffle(players, rand);
        // create a list offset by one
        List<Player> offset = new ArrayList<>(players.size());
        offset.addAll(players.subList(1, players.size()));
        offset.add(players.get(0));
        // get teleport destinations
        Map<Player, Location> destinations = new HashMap<>(players.size());
        for (int i = 0; i < players.size(); ++i)
            destinations.put(players.get(i), offset.get(i).getLocation());
        // teleport
        Bukkit.getScheduler().runTask(plugin, () -> destinations.forEach(Entity::teleportAsync));
        return request.buildResponse().type(Response.ResultType.SUCCESS);
    }
}
