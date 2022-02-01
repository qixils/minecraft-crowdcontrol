package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge8.TimedCommand;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.math.vector.Vector3d;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static dev.qixils.crowdcontrol.common.CommandConstants.FREEZE_DURATION;

@Getter
public final class CameraLockCommand extends TimedCommand {
	private final String effectName = "camera_lock";
	private final String displayName = "Camera Lock";

	public CameraLockCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	public @NotNull Duration getDuration() {
		return FREEZE_DURATION;
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull ServerPlayer> ignored, @NotNull Request request) {
		AtomicReference<ScheduledTask> task = new AtomicReference<>();

		new TimedEffect.Builder()
				.request(request)
				.effectGroup("camera_lock")
				.duration(getDuration())
				.startCallback($ -> {
					List<ServerPlayer> players = plugin.getPlayers(request);
					Map<UUID, Vector3d> rotations = new HashMap<>(players.size());
					for (Player player : players)
						rotations.put(player.uniqueId(), player.rotation());
					task.set(plugin.getSyncScheduler().submit(Task.builder()
							.delay(Ticks.of(1))
							.interval(Ticks.of(1))
							.execute(() -> players.forEach(player -> {
								if (!rotations.containsKey(player.uniqueId()))
									return;
								Vector3d rotation = rotations.get(player.uniqueId());
								Vector3d currentRot = player.rotation();
								if (rotation.equals(currentRot))
									return;
								player.setRotation(rotation);
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
