package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge7.TimedVoidCommand;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.FREEZE_DURATION;

@Getter
public final class FreezeCommand extends TimedVoidCommand {
	private final String effectName = "freeze";

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

		new TimedEffect.Builder()
				.request(request)
				.duration(getDuration(request))
				.startCallback($ -> {
					List<Player> players = plugin.getPlayers(request);
					Map<UUID, Location<World>> locations = new HashMap<>(players.size());
					players.forEach(player -> locations.put(player.getUniqueId(), player.getLocation()));
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
				.completionCallback($ -> task.get().cancel())
				.build().queue();
	}
}
