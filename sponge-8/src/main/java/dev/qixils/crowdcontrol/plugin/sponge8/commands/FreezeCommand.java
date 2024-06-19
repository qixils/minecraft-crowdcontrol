package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.common.EventListener;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge8.TimedVoidCommand;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.server.ServerLocation;

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
	public void voidExecute(@NotNull List<@NotNull ServerPlayer> ignored, @NotNull Request request) {
		AtomicReference<ScheduledTask> task = new AtomicReference<>();
		Set<UUID> uuids = new HashSet<>();

		new TimedEffect.Builder()
				.request(request)
				.effectGroup("walk")
				.duration(getDuration(request))
				.startCallback(timedEffect -> {
					List<ServerPlayer> players = plugin.getPlayers(request);
					Map<UUID, ServerLocation> locations = new HashMap<>(players.size());
					players.forEach(player -> {
						UUID uuid = player.uniqueId();
						uuids.add(uuid);
						timedEffects.put(uuid, timedEffect);
						locations.put(uuid, player.serverLocation());
					});
					// TODO: smoother freeze (stop mid-air jitter by telling client it's flying?)
					task.set(plugin.getSyncScheduler().submit(Task.builder()
							.delay(Ticks.of(1))
							.interval(Ticks.of(1))
							.execute(() -> players.forEach(player -> {
								if (!locations.containsKey(player.uniqueId()))
									return;

								ServerLocation location = locations.get(player.uniqueId());
								ServerLocation playerLoc = player.serverLocation();
								if (!location.world().equals(playerLoc.world()))
									return;
								if (location.equals(playerLoc))
									return;
								player.setLocation(location);
							}))
							.plugin(plugin.getPluginContainer())
							.build()));
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
		Living entity = event.entity();
		if (!(entity instanceof Player)) return;

		UUID uuid = entity.uniqueId();
		TimedEffect effect = timedEffects.get(uuid);
		if (effect == null) return;

		try {
			effect.complete();
		} catch (Exception e) {
			getPlugin().getSLF4JLogger().warn("Failed to stop freeze effect", e);
		}
	}
}
