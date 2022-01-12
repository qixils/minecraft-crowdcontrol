package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.TimedCommand;
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

@Getter
public final class CameraLockToGroundCommand extends TimedCommand {
	private final Duration duration = Duration.ofSeconds(7);
	private final String effectName = "camera_lock_to_ground";
	private final String displayName = "Camera Lock To Ground";

	public CameraLockToGroundCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull Player> ignored, @NotNull Request request) {
		AtomicReference<BukkitTask> task = new AtomicReference<>();
		new TimedEffect.Builder()
				.request(request)
				.effectGroup("camera_lock")
				.duration(duration)
				.startCallback($ -> {
					List<Player> players = plugin.getPlayers(request);
					task.set(Bukkit.getScheduler().runTaskTimer(plugin, () -> players.forEach(player -> {
						Location playerLoc = player.getLocation();
						if (playerLoc.getPitch() < 89.99) {
							playerLoc.setPitch(90);
							player.teleport(playerLoc);
						}
					}), 1, 1));
					announce(players, request);
					return null;
				})
				.completionCallback($ -> task.get().cancel())
				.build().queue();
	}
}
