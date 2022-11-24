package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import com.flowpowered.math.vector.Vector3d;
import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge7.TimedVoidCommand;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.FREEZE_DURATION;

@Getter
public class CameraLockToSkyCommand extends TimedVoidCommand {
	private final String effectName = "camera_lock_to_sky";

	public CameraLockToSkyCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	public @NotNull Duration getDefaultDuration() {
		return FREEZE_DURATION;
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull Player> ignored, @NotNull Request request) {
		AtomicReference<Task> task = new AtomicReference<>();
		new TimedEffect.Builder()
				.request(request)
				.effectGroup("camera_lock")
				.duration(getDuration(request))
				.startCallback($ -> {
					List<Player> players = plugin.getPlayers(request);
					task.set(Task.builder()
							.intervalTicks(1)
							.delayTicks(1)
							.execute(() -> players.forEach(player -> {
								Vector3d rotation = player.getRotation();
								if (rotation.getX() > -89.99)
									player.setRotation(new Vector3d(-90, rotation.getY(), rotation.getZ()));
							}))
							.submit(plugin));
					playerAnnounce(players, request);
					return null;
				})
				.completionCallback($ -> task.get().cancel())
				.build().queue();
	}
}
