package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.TimedCommand;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public final class CameraLockToGroundCommand extends TimedCommand {
    public CameraLockToGroundCommand(CrowdControlPlugin plugin) {
        super(plugin);
    }
    private static final Duration DURATION = Duration.ofSeconds(20);

    @Getter
    private final String effectName = "camera_lock_to_ground";
    @Getter
    private final String displayName = "Camera Lock To Ground";

    @Override
    public @NotNull Duration getDuration() {
        return DURATION;
    }

    @Override
    protected void voidExecute(@NotNull List<@NotNull Player> players, @NotNull Request request) {
        AtomicReference<BukkitTask> task = new AtomicReference<>();
        new TimedEffect(request, "camera_lock", DURATION, $ -> {
            task.set(Bukkit.getScheduler().runTaskTimer(plugin, () -> players.forEach(player -> {
                Location playerLoc = player.getLocation();
                if (playerLoc.getPitch() < 89.99) {
                    playerLoc.setPitch(90);
                    player.teleport(playerLoc);
                }
            }), 1, 1));
            announce(request);
        }, $ -> task.get().cancel()).queue();
    }
}
