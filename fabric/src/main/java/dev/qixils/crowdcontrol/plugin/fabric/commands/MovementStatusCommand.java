package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.common.components.MovementStatusType;
import dev.qixils.crowdcontrol.common.components.MovementStatusValue;
import dev.qixils.crowdcontrol.common.util.ComparableUtil;
import dev.qixils.crowdcontrol.common.util.SemVer;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.TimedVoidCommand;
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
	private final MovementStatusType type;
	private final MovementStatusValue value;
	private final boolean clientOnly;
	private final SemVer minimumModVersion;

	public MovementStatusCommand(FabricCrowdControlPlugin plugin, String effectName, String effectGroup, Duration defaultDuration, MovementStatusType type, MovementStatusValue value, boolean clientOnly) {
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

	public MovementStatusCommand(FabricCrowdControlPlugin plugin, String effectName, Duration defaultDuration, MovementStatusType type, MovementStatusValue value, boolean clientOnly) {
		this(plugin, effectName, effectName, defaultDuration, type, value, clientOnly);
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull ServerPlayer> ignored, @NotNull Request request) {
		AtomicReference<List<ServerPlayer>> atomicPlayers = new AtomicReference<>();
		new TimedEffect.Builder()
				.request(request)
				.effectGroup(effectGroup)
				.duration(getDuration(request))
				.startCallback($ -> {
					List<ServerPlayer> players = plugin.getPlayers(request);
					if (clientOnly)
						players.removeIf(player -> plugin.getModVersion(player).orElse(SemVer.ZERO).isLessThan(minimumModVersion));
					atomicPlayers.set(players);

					if (players.isEmpty())
						return request.buildResponse()
								.type(Response.ResultType.FAILURE)
								.message("No targetable players online");

					for (Player player : players)
						player.cc$setMovementStatus(type, value);
					playerAnnounce(players, request);

					return null; // success
				})
				.completionCallback($ -> {
					for (Player player : atomicPlayers.get())
						player.cc$setMovementStatus(type, MovementStatusValue.ALLOWED);
				})
				.build().queue();
	}

	public static MovementStatusCommand disableJumping(FabricCrowdControlPlugin plugin) {
		return new MovementStatusCommand(plugin, "disable_jumping", DISABLE_JUMPING_DURATION, MovementStatusType.JUMP, MovementStatusValue.DENIED, false);
	}

	public static MovementStatusCommand invertControls(FabricCrowdControlPlugin plugin) {
		return new MovementStatusCommand(plugin, "invert_wasd", "walk", INVERT_CONTROLS_DURATION, MovementStatusType.WALK, MovementStatusValue.INVERTED, true);
	}

	public static MovementStatusCommand invertCamera(FabricCrowdControlPlugin plugin) {
		return new MovementStatusCommand(plugin, "invert_look", "look", INVERT_CONTROLS_DURATION, MovementStatusType.LOOK, MovementStatusValue.INVERTED, true);
	}
}
