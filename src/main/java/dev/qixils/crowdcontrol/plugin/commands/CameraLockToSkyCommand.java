package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.VoidCommand;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public final class CameraLockToSkyCommand extends VoidCommand {
    public CameraLockToSkyCommand(CrowdControlPlugin plugin) {
        super(plugin);
    }
    private static final Duration DURATION = Duration.ofSeconds(20);

    @Getter
    private final String effectName = "camera_lock_to_sky";
    @Getter
    private final String displayName = "Camera Lock To Sky (" + DURATION.toSeconds() + "s)";

    @Override
    public void voidExecute(@NotNull Request request) {
        AtomicReference<BukkitTask> task = new AtomicReference<>();
        new TimedEffect(Objects.requireNonNull(plugin.getCrowdControl()), request, DURATION, $ -> {
            task.set(Bukkit.getScheduler().runTaskTimer(plugin, () -> CrowdControlPlugin.getPlayers().forEach(player -> {
                Location playerLoc = player.getLocation();
                if (playerLoc.getPitch() > -89.99) {
                    playerLoc.setPitch(-90);
                    player.teleport(playerLoc);
                }
            }), 1, 1));
            announce(request);
        }, $ -> task.get().cancel()).queue();
    }
}
