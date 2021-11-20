package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.TimedCommand;
import dev.qixils.crowdcontrol.plugin.utils.PlayerListWrapper;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public final class CameraLockCommand extends TimedCommand {
    public CameraLockCommand(CrowdControlPlugin plugin) {
        super(plugin);
    }
    private static final Duration DURATION = Duration.ofSeconds(7);

    @Getter
    private final String effectName = "camera_lock";
    @Getter
    private final String displayName = "Camera Lock";

    @Override
    public @NotNull Duration getDuration() {
        return DURATION;
    }

    @Override
    protected void voidExecute(@NotNull List<@NotNull Player> ignored, @NotNull Request request) {
        AtomicReference<BukkitTask> task = new AtomicReference<>();
        PlayerListWrapper wrapper = new PlayerListWrapper(request, players -> {
            Map<UUID, Location> locations = new HashMap<>();
            for (Player player : players)
                locations.put(player.getUniqueId(), player.getLocation());
            task.set(Bukkit.getScheduler().runTaskTimer(plugin, () -> players.forEach(player -> {
                if (!locations.containsKey(player.getUniqueId()))
                    return;

                Location location = locations.get(player.getUniqueId());
                Location playerLoc = player.getLocation();
                if (!location.getWorld().equals(playerLoc.getWorld()))
                    return;

                if (location.getPitch() != playerLoc.getPitch() || location.getYaw() != playerLoc.getYaw())
                    player.teleport(new Location(location.getWorld(), playerLoc.getX(), playerLoc.getY(), playerLoc.getZ(), location.getYaw(), location.getPitch()));
            }), 1, 1));
            announce(players, request);
        });

        new TimedEffect(request, "camera_lock", DURATION,
                $ -> CrowdControlPlugin.getPlayers(request).whenComplete(wrapper),
                $ -> task.get().cancel()
        ).queue();
    }
}
