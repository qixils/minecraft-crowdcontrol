package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.ChatCommand;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SwapCommand extends ChatCommand {
    public SwapCommand(CrowdControlPlugin plugin) {
        super(plugin);
    }

    @Override
    public int getCooldownSeconds() {
        return 60*20;
    }

    @Override
    public @NotNull String getCommand() {
        return "swap";
    }

    @Override
    public boolean execute(String authorName, List<Player> players, String... args) {
        if (players.size() < 2) {
            return false;
        }

        // get shuffled list of players
        List<Player> playersList = new ArrayList<>(players);
        Collections.shuffle(playersList, rand);
        // create a list offset by one
        List<Player> offset = playersList.subList(1, players.size());
        offset.add(playersList.get(0));
        // get teleport destinations
        Map<Player, Location> destinations = new HashMap<>();
        for (int i = 0; i < players.size(); ++i) {
            destinations.put(playersList.get(i), offset.get(i).getLocation());
        }
        // teleport
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<Player, Location> entry : destinations.entrySet()) {
                    entry.getKey().teleport(entry.getValue());
                }
            }
        }.runTask(plugin);
        return true;
    }
}
