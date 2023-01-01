package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.TimedVoidCommand;
import dev.qixils.crowdcontrol.plugin.fabric.event.Join;
import dev.qixils.crowdcontrol.plugin.fabric.event.Listener;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.Components;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.MovementStatus;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.DISABLE_JUMPING_DURATION;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.INVERT_CONTROLS_DURATION;

@Getter
public class MovementStatusCommand extends TimedVoidCommand {
	private final String effectName;
	private final String effectGroup;
	private final Duration defaultDuration;
	private final MovementStatus.Type type;
	private final MovementStatus.Value value;
	private final boolean clientSide;

	public MovementStatusCommand(FabricCrowdControlPlugin plugin, String effectName, String effectGroup, Duration defaultDuration, MovementStatus.Type type, MovementStatus.Value value, boolean clientSide) {
		super(plugin);
		this.effectName = effectName;
		this.effectGroup = effectGroup;
		this.defaultDuration = defaultDuration;
		this.type = type;
		this.value = value;
		this.clientSide = clientSide;
	}

	public MovementStatusCommand(FabricCrowdControlPlugin plugin, String effectName, Duration defaultDuration, MovementStatus.Type type, MovementStatus.Value value, boolean clientSide) {
		this(plugin, effectName, effectName, defaultDuration, type, value, clientSide);
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull ServerPlayer> ignored, @NotNull Request request) {
		AtomicReference<List<ServerPlayer>> players = new AtomicReference<>();
		new TimedEffect.Builder()
				.request(request)
				.effectGroup("freeze") // TODO: support freezing walk & look at the same time
				.duration(getDuration(request))
				.startCallback($ -> {
					players.set(plugin.getPlayers(request));
					if (players.get().isEmpty())
						return request.buildResponse()
								.type(Response.ResultType.FAILURE)
								.message("No players online");

					// TODO: handle client-side

					for (Player player : players.get())
						Components.MOVEMENT_STATUS.get(player).set(type, value);
					playerAnnounce(players.get(), request);

					return null; // success
				})
				.completionCallback($ -> {
					for (Player player : players.get())
						Components.MOVEMENT_STATUS.get(player).set(type, MovementStatus.Value.ALLOWED);
				})
				.build().queue();
	}

	public static MovementStatusCommand disableJumping(FabricCrowdControlPlugin plugin) {
		return new MovementStatusCommand(plugin, "disable_jumping", DISABLE_JUMPING_DURATION, MovementStatus.Type.JUMP, MovementStatus.Value.DENIED, false);
	}

	public static MovementStatusCommand invertControls(FabricCrowdControlPlugin plugin) {
		return new MovementStatusCommand(plugin, "invert_wasd", "walk", INVERT_CONTROLS_DURATION, MovementStatus.Type.WALK, MovementStatus.Value.INVERTED, true);
	}

	public static MovementStatusCommand invertCamera(FabricCrowdControlPlugin plugin) {
		return new MovementStatusCommand(plugin, "invert_look", "look", INVERT_CONTROLS_DURATION, MovementStatus.Type.LOOK, MovementStatus.Value.INVERTED, true);
	}

	public static final class Manager {
		@Listener
		public void onJoin(Join event) {
			MovementStatus data = Components.MOVEMENT_STATUS.get(event.player());
			for (MovementStatus.Type type : MovementStatus.Type.values())
				data.rawSet(type, MovementStatus.Value.ALLOWED);
			data.sync();
		}
	}
}
