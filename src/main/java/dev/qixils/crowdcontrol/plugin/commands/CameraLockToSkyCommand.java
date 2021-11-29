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

public final class CameraLockToSkyCommand extends TimedCommand {
    private static final Duration DURATION = Duration.ofSeconds(7);
    @Getter
    private final String effectName = "camera_lock_to_sky";
    @Getter
    private final String displayName = "Camera Lock To Sky";

    public CameraLockToSkyCommand(CrowdControlPlugin plugin) {
        super(plugin);
    }

    @Override
    public @NotNull Duration getDuration() {
        return DURATION;
    }

    @Override
    public void voidExecute(@NotNull List<@NotNull Player> ignored, @NotNull Request request) {
        AtomicReference<BukkitTask> task = new AtomicReference<>();

        new TimedEffect(request, "camera_lock", DURATION,
                $ -> {
                    List<Player> players = plugin.getPlayers(request);
                    task.set(Bukkit.getScheduler().runTaskTimer(plugin, () -> players.forEach(player -> {
                        Location playerLoc = player.getLocation();
                        if (playerLoc.getPitch() > -89.99) {
                            playerLoc.setPitch(-90);
                            player.teleport(playerLoc);
                        }
                    }), 1, 1));
                    announce(players, request);
                },
                $ -> task.get().cancel()
        ).queue();
    }
}
