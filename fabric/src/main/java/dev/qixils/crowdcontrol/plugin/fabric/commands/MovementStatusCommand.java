package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.TimedVoidCommand;
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
import java.util.function.BiConsumer;
import java.util.function.Function;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.DISABLE_JUMPING_DURATION;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.INVERT_CONTROLS_DURATION;

@Getter
public class MovementStatusCommand extends TimedVoidCommand {
	private final String effectName;
	private final Duration defaultDuration;
	private final Function<MovementStatus, BiConsumer<MovementStatus.Type, Boolean>> setterGetter;
	private final MovementStatus.Type type;
	private final boolean clientSide;

	public MovementStatusCommand(FabricCrowdControlPlugin plugin, String effectName, Duration defaultDuration, Function<MovementStatus, BiConsumer<MovementStatus.Type, Boolean>> setterGetter, MovementStatus.Type type, boolean clientSide) {
		super(plugin);
		this.effectName = effectName;
		this.defaultDuration = defaultDuration;
		this.setterGetter = setterGetter;
		this.type = type;
		this.clientSide = clientSide;
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull ServerPlayer> ignored, @NotNull Request request) {
		AtomicReference<List<ServerPlayer>> players = new AtomicReference<>();
		new TimedEffect.Builder().request(request)
				.duration(getDuration(request))
				.startCallback($ -> {
					players.set(plugin.getPlayers(request));
					if (players.get().isEmpty())
						return request.buildResponse()
								.type(Response.ResultType.FAILURE)
								.message("No players online");

					// TODO: handle client-side

					for (Player player : players.get())
						setterGetter.apply(Components.MOVEMENT_STATUS.get(player)).accept(type, true);
					playerAnnounce(players.get(), request);

					return null; // success
				})
				.completionCallback($ -> {
					for (Player player : players.get())
						setterGetter.apply(Components.MOVEMENT_STATUS.get(player)).accept(type, false);
				})
				.build().queue();
	}

	public static MovementStatusCommand disableJumping(FabricCrowdControlPlugin plugin) {
		return new MovementStatusCommand(plugin, "disable_jumping", DISABLE_JUMPING_DURATION, status -> status::setProhibited, MovementStatus.Type.JUMP, false);
	}

	public static MovementStatusCommand invertControls(FabricCrowdControlPlugin plugin) {
		return new MovementStatusCommand(plugin, "invert_wasd", INVERT_CONTROLS_DURATION, status -> status::setInverted, MovementStatus.Type.WALK, true);
	}

	public static MovementStatusCommand invertCamera(FabricCrowdControlPlugin plugin) {
		return new MovementStatusCommand(plugin, "invert_look", INVERT_CONTROLS_DURATION, status -> status::setInverted, MovementStatus.Type.LOOK, true);
	}
}
