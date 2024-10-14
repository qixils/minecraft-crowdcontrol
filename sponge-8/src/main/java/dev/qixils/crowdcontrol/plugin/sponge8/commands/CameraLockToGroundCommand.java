package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge8.TimedVoidCommand;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.math.vector.Vector3d;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.FREEZE_DURATION;

@Getter
public class CameraLockToGroundCommand extends TimedVoidCommand {
	private final String effectName = "camera_lock_to_ground";

	public CameraLockToGroundCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	public @NotNull Duration getDefaultDuration() {
		return FREEZE_DURATION;
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull ServerPlayer> ignored, @NotNull Request request) {
		AtomicReference<ScheduledTask> task = new AtomicReference<>();
		new TimedEffect.Builder()
				.request(request)
				.effectGroup("look")
				.duration(getDuration(request))
				.startCallback($ -> {
					List<ServerPlayer> players = plugin.getPlayers(request);
					task.set(plugin.getSyncScheduler().submit(Task.builder()
							.interval(Ticks.of(1))
							.delay(Ticks.of(1))
							.execute(() -> players.forEach(player -> {
								Vector3d rotation = player.rotation();
								if (rotation.x() < 89.99)
									player.setRotation(new Vector3d(90, rotation.y(), rotation.z()));
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
