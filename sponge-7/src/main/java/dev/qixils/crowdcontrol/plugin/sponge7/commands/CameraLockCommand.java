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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.FREEZE_DURATION;

@Getter
public final class CameraLockCommand extends TimedVoidCommand {
	private final String effectName = "camera_lock";

	public CameraLockCommand(SpongeCrowdControlPlugin plugin) {
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
					Map<UUID, Vector3d> rotations = new HashMap<>(players.size());
					for (Player player : players)
						rotations.put(player.getUniqueId(), player.getRotation());
					task.set(Task.builder()
							.delayTicks(1)
							.intervalTicks(1)
							.execute(() -> players.forEach(player -> {
								if (!rotations.containsKey(player.getUniqueId()))
									return;
								Vector3d rotation = rotations.get(player.getUniqueId());
								Vector3d currentRot = player.getRotation();
								if (rotation.equals(currentRot))
									return;
								player.setRotation(rotation);
							}))
							.submit(plugin));
					playerAnnounce(players, request);
					return null;
				})
				.completionCallback($ -> task.get().cancel())
				.build().queue();
	}
}
