package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.common.util.ComparableUtil;
import dev.qixils.crowdcontrol.common.util.SemVer;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.TimedVoidCommand;
import dev.qixils.crowdcontrol.plugin.fabric.event.Join;
import dev.qixils.crowdcontrol.plugin.fabric.event.Jump;
import dev.qixils.crowdcontrol.plugin.fabric.event.Listener;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.Components;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.MovementStatus;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
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
	private final boolean clientOnly;
	private final SemVer minimumModVersion;

	public MovementStatusCommand(FabricCrowdControlPlugin plugin, String effectName, String effectGroup, Duration defaultDuration, MovementStatus.Type type, MovementStatus.Value value, boolean clientOnly) {
		super(plugin);
		this.effectName = effectName;
		this.effectGroup = effectGroup;
		this.defaultDuration = defaultDuration;
		this.type = type;
		this.value = value;
		this.clientOnly = clientOnly;
		if (clientOnly)
			this.minimumModVersion = ComparableUtil.max(type.addedIn(), value.addedIn());
		else
			this.minimumModVersion = SemVer.ZERO;
	}

	public MovementStatusCommand(FabricCrowdControlPlugin plugin, String effectName, Duration defaultDuration, MovementStatus.Type type, MovementStatus.Value value, boolean clientOnly) {
		this(plugin, effectName, effectName, defaultDuration, type, value, clientOnly);
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull ServerPlayerEntity> ignored, @NotNull Request request) {
		AtomicReference<List<ServerPlayerEntity>> atomicPlayers = new AtomicReference<>();
		new TimedEffect.Builder()
				.request(request)
				.effectGroup(effectGroup)
				.duration(getDuration(request))
				.startCallback($ -> {
					List<ServerPlayerEntity> players = plugin.getPlayers(request);
					if (clientOnly)
						players.removeIf(player -> plugin.getModVersion(player).orElse(SemVer.ZERO).isLessThan(minimumModVersion));
					atomicPlayers.set(players);

					if (players.isEmpty())
						return request.buildResponse()
								.type(Response.ResultType.FAILURE)
								.message("No targetable players online");

					for (PlayerEntity player : players)
						Components.MOVEMENT_STATUS.get(player).set(type, value);
					playerAnnounce(players, request);

					return null; // success
				})
				.completionCallback($ -> {
					for (PlayerEntity player : atomicPlayers.get())
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

		@Listener
		public void onJump(Jump event) {
			PlayerEntity player = event.player();
			MovementStatus status = Components.MOVEMENT_STATUS.get(player);
			boolean cantJump = status.get(MovementStatus.Type.JUMP) == MovementStatus.Value.DENIED;
			boolean cantWalk = status.get(MovementStatus.Type.WALK) == MovementStatus.Value.DENIED;
			if (cantJump || cantWalk) {
				event.cancel();
				if (!event.isClientSide() && player instanceof ServerPlayerEntity sPlayer /* not necessary for clients */ && !cantWalk /* avoids teleporting twice */) {
					sPlayer.networkHandler.requestTeleport(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
				}
			}
		}
	}
}
