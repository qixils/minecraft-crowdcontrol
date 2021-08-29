package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.Command;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
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

public class CameraLockCommand extends Command {
    public CameraLockCommand(CrowdControlPlugin plugin) {
        super(plugin);
    }

    @Getter
    private final String effectName = "camera_lock";
    @Getter
    private final String displayName = "Camera Lock";

    @Override
    public Response.@NotNull Result execute(@NotNull Request request) {
        Map<UUID, Location> locations = new HashMap<>();
        CrowdControlPlugin.getPlayers().forEach(player -> locations.put(player.getUniqueId(), player.getLocation()));
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> CrowdControlPlugin.getPlayers().forEach(player -> {
            if (!locations.containsKey(player.getUniqueId()))
                return;

            Location location = locations.get(player.getUniqueId());
            Location playerLoc = player.getLocation();
            if (!location.getWorld().equals(playerLoc.getWorld()))
                return;

            if (location.getPitch() != playerLoc.getPitch() || location.getYaw() != playerLoc.getYaw())
                player.teleport(new Location(location.getWorld(), playerLoc.getX(), playerLoc.getY(), playerLoc.getZ(), location.getYaw(), location.getPitch()));
        }), 1, 1);
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task::cancel, 20*7);
        return Response.Result.SUCCESS;
    }
}
