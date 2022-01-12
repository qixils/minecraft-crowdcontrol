package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import com.flowpowered.math.vector.Vector3d;
import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge7.TimedCommand;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Getter
public class CameraLockToSkyCommand extends TimedCommand {
	private final Duration duration = Duration.ofSeconds(7);
	private final String effectName = "camera_lock_to_sky";
	private final String displayName = "Camera Lock To Sky";

	public CameraLockToSkyCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull Player> ignored, @NotNull Request request) {
		AtomicReference<Task> task = new AtomicReference<>();
		new TimedEffect.Builder()
				.request(request)
				.effectGroup("camera_lock")
				.duration(duration)
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