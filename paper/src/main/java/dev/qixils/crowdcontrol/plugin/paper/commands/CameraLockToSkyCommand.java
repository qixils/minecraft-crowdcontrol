package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.TimedVoidCommand;
import dev.qixils.crowdcontrol.socket.Request;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.FREEZE_DURATION;

@Getter
public final class CameraLockToSkyCommand extends TimedVoidCommand {
	private final String effectName = "camera_lock_to_sky";

	public CameraLockToSkyCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	public @NotNull Duration getDefaultDuration() {
		return FREEZE_DURATION;
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull Player> ignored, @NotNull Request request) {
		AtomicReference<ScheduledTask> task = new AtomicReference<>();

		new TimedEffect.Builder()
				.request(request)
				.effectGroup("look")
				.duration(getDuration(request))
				.startCallback($ -> {
					List<Player> players = plugin.getPlayers(request);
					task.set(Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, $$ -> players.forEach(player -> player.getScheduler().run(plugin, $$$ -> {
						Location playerLoc = player.getLocation();
						if (playerLoc.getPitch() > -89.99) {
							playerLoc.setPitch(-90);
							player.teleportAsync(playerLoc);
						}
					}, null)), 1, 1));
					announce(players, request);
					return null;
				})
				.completionCallback($ -> task.get().cancel())
				.build().queue();
	}
}
