package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.CrowdControl;
import dev.qixils.crowdcontrol.plugin.Command;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CameraLockCommand extends Command {
    public CameraLockCommand(CrowdControlPlugin plugin) {
        super(plugin);
    }

    @Getter
    private final String effectName = "camera_lock";
    @Getter
    private final String displayName = "Camera Lock";

    @Override
    public @NotNull CompletableFuture<Response.@NotNull Builder> execute(@NotNull Request request) {
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
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            task.cancel();
            CrowdControl cc = plugin.getCrowdControl();
            if (cc != null) cc.dispatchResponse(new Response.Builder(request).type(Response.ResultType.FINISHED).build());
        }, 20*7);
        return CompletableFuture.completedFuture(Response.builder().type(Response.ResultType.SUCCESS).timeRemaining(Duration.ofSeconds(7)));
    }
}
