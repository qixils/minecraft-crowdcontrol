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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.FREEZE_DURATION;

@Getter
public final class CameraLockCommand extends TimedVoidCommand {
	private final String effectName = "camera_lock";

	public CameraLockCommand(PaperCrowdControlPlugin plugin) {
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
					Map<UUID, Location> locations = new HashMap<>(players.size());
					for (Player player : players)
						locations.put(player.getUniqueId(), player.getLocation());
					task.set(Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, $$ -> players.forEach(player -> player.getScheduler().run(plugin, $$$ -> {
						if (!locations.containsKey(player.getUniqueId()))
							return;

						Location location = locations.get(player.getUniqueId());
						Location playerLoc = player.getLocation();
						if (!location.getWorld().equals(playerLoc.getWorld()))
							return;

						if (location.getPitch() != playerLoc.getPitch() || location.getYaw() != playerLoc.getYaw())
							player.teleportAsync(new Location(location.getWorld(), playerLoc.getX(), playerLoc.getY(), playerLoc.getZ(), location.getYaw(), location.getPitch()));
					}, null)), 1, 1));
					announce(players, request);
					return null;
				})
				.completionCallback($ -> task.get().cancel())
				.build().queue();
	}
}
