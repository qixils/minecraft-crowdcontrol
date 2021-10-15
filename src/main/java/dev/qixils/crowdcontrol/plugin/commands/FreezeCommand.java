package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.TimedCommand;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public final class FreezeCommand extends TimedCommand {
    public FreezeCommand(CrowdControlPlugin plugin) {
        super(plugin);
    }
    private static final Duration DURATION = Duration.ofSeconds(7);

    @Getter
    private final String effectName = "freeze";
    @Getter
    private final String displayName = "Freeze";

    @Override
    public @NotNull Duration getDuration() {
        return DURATION;
    }

    @Override
    public void voidExecute(@NotNull Request request) {
        AtomicReference<BukkitTask> task = new AtomicReference<>();
        new TimedEffect(Objects.requireNonNull(plugin.getCrowdControl()), request, DURATION, $ -> {
            Map<UUID, Location> locations = new HashMap<>();
            CrowdControlPlugin.getPlayers().forEach(player -> locations.put(player.getUniqueId(), player.getLocation()));
            task.set(Bukkit.getScheduler().runTaskTimer(plugin, () -> CrowdControlPlugin.getPlayers().forEach(player -> {
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
            }), 1, 1));
            announce(request);
        }, $ -> task.get().cancel()).queue();
    }
}
