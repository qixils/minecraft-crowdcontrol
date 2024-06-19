package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.TimedVoidCommand;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.FREEZE_DURATION;

@Getter
public final class FreezeCommand extends TimedVoidCommand implements Listener {
	private final String effectName = "freeze";
	private final Map<UUID, TimedEffect> timedEffects = new HashMap<>();

	public FreezeCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	public @NotNull Duration getDefaultDuration() {
		return FREEZE_DURATION;
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull Player> ignored, @NotNull Request request) {
		AtomicReference<BukkitTask> task = new AtomicReference<>();
		Set<UUID> uuids = new HashSet<>();

		new TimedEffect.Builder()
				.request(request)
				.effectGroup("walk")
				.duration(getDuration(request))
				.startCallback(effect -> {
					List<Player> players = plugin.getPlayers(request);

					Map<UUID, Location> locations = new HashMap<>(players.size());
					for (Player player : players) {
						UUID uuid = player.getUniqueId();
						uuids.add(uuid);
						locations.put(uuid, player.getLocation());
						timedEffects.put(uuid, effect);
					}
					// TODO: smoother freeze (stop mid-air jitter by telling client it's flying? zero gravity?)
					task.set(Bukkit.getScheduler().runTaskTimer(plugin, () -> players.forEach(player -> {
						if (!locations.containsKey(player.getUniqueId()))
							return;

						Location location = locations.get(player.getUniqueId());
						Location playerLoc = player.getLocation();
						if (!location.getWorld().equals(playerLoc.getWorld()))
							return;

						if (location.getX() != playerLoc.getX() || location.getY() != playerLoc.getY() || location.getZ() != playerLoc.getZ()) {
							// preserve rotation
							playerLoc.set(location.getX(), location.getY(), location.getZ());
							player.teleport(playerLoc);
						}
					}), 1, 1));
					announce(players, request);
					return null;
				})
				.completionCallback($ -> {
					uuids.forEach(timedEffects::remove);
					task.get().cancel();
				})
				.build().queue();
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onDeath(PlayerDeathEvent event) {
		UUID uuid = event.getEntity().getUniqueId();
		TimedEffect effect = timedEffects.get(uuid);
		if (effect == null) return;

		try {
			effect.complete();
		} catch (Exception e) {
			getPlugin().getSLF4JLogger().warn("Failed to stop freeze effect", e);
		}
	}
}
