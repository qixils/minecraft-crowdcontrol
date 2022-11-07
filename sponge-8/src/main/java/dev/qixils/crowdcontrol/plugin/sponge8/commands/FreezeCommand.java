package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge8.TimedCommand;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.server.ServerLocation;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.FREEZE_DURATION;

@Getter
public final class FreezeCommand extends TimedCommand {
	private final String effectName = "freeze";

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

		new TimedEffect.Builder()
				.request(request)
				.duration(getDuration(request))
				.startCallback($ -> {
					List<ServerPlayer> players = plugin.getPlayers(request);
					Map<UUID, ServerLocation> locations = new HashMap<>(players.size());
					players.forEach(player -> locations.put(player.uniqueId(), player.serverLocation()));
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
				.completionCallback($ -> task.get().cancel())
				.build().queue();
	}
}
