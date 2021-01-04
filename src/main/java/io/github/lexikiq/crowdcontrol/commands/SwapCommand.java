package io.github.lexikiq.crowdcontrol.commands;

import io.github.lexikiq.crowdcontrol.ChatCommand;
import io.github.lexikiq.crowdcontrol.CrowdControl;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SwapCommand extends ChatCommand {
    public SwapCommand(CrowdControl plugin) {
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
