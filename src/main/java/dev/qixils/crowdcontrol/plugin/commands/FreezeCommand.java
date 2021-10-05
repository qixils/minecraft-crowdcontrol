package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FreezeCommand extends ImmediateCommand {
    public FreezeCommand(CrowdControlPlugin plugin) {
        super(plugin);
    }

    @Getter
    private final String effectName = "freeze";
    @Getter
    private final String displayName = "Freeze";

    @Override
    public Response.@NotNull Builder executeImmediately(@NotNull Request request) {
        Map<UUID, Location> locations = new HashMap<>();
        CrowdControlPlugin.getPlayers().forEach(player -> locations.put(player.getUniqueId(), player.getLocation()));
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            CrowdControlPlugin.getPlayers().forEach(player -> {
                if (!locations.containsKey(player.getUniqueId()))
                    return;

                Location location = locations.get(player.getUniqueId());
                Location playerLoc = player.getLocation();
                if (!location.getWorld().equals(playerLoc.getWorld()))
                    return;

                if (location.getX() != playerLoc.getX() || location.getY() != playerLoc.getY() || location.getZ() != playerLoc.getZ()) {
                    playerLoc.set(location.getX(), location.getY(), location.getZ());
                    player.teleport(playerLoc);
                }
            });
        }, 1, 1);
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task::cancel, 20*7);
        return Response.builder().type(Response.ResultType.SUCCESS);
    }
}
