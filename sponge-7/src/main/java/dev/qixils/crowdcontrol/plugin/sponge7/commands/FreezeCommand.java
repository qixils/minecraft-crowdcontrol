package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.common.EventListener;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge7.TimedVoidCommand;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.FREEZE_DURATION;

@Getter
@EventListener
public final class FreezeCommand extends TimedVoidCommand {
	private final String effectName = "freeze";
	private final Map<UUID, TimedEffect> timedEffects = new HashMap<>();

	public FreezeCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public @NotNull Duration getDefaultDuration() {
		return FREEZE_DURATION;
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull Player> ignored, @NotNull Request request) {
		AtomicReference<Task> task = new AtomicReference<>();
		Set<UUID> uuids = new HashSet<>();

		new TimedEffect.Builder()
				.request(request)
				.effectGroup("walk")
				.duration(getDuration(request))
				.startCallback(timedEffect -> {
					List<Player> players = plugin.getPlayers(request);
					Map<UUID, Location<World>> locations = new HashMap<>(players.size());
					players.forEach(player -> {
						UUID uuid = player.getUniqueId();
						uuids.add(uuid);
						timedEffects.put(uuid, timedEffect);
						locations.put(uuid, player.getLocation());
					});
					// TODO: smoother freeze (stop mid-air jitter by telling client it's flying?)
					task.set(Task.builder()
							.delayTicks(1)
							.intervalTicks(1)
							.execute(() -> players.forEach(player -> {
								if (!locations.containsKey(player.getUniqueId()))
									return;

								Location<World> location = locations.get(player.getUniqueId());
								Location<World> playerLoc = player.getLocation();
								if (!location.getExtent().equals(playerLoc.getExtent()))
									return;
								if (location.equals(playerLoc))
									return;
								player.setLocation(location);
							}))
							.submit(plugin));
					playerAnnounce(players, request);
					return null;
				})
				.completionCallback($ -> {
					uuids.forEach(timedEffects::remove);
					task.get().cancel();
				})
				.build().queue();
	}

	@Listener
	public void onDeath(DestructEntityEvent.Death event) {
		Living entity = event.getTargetEntity();
		if (!(entity instanceof Player)) return;

		UUID uuid = entity.getUniqueId();
		TimedEffect effect = timedEffects.get(uuid);
		if (effect == null) return;

		try {
			effect.complete();
		} catch (Exception e) {
			getPlugin().getSLF4JLogger().warn("Failed to stop freeze effect", e);
		}
	}
}
