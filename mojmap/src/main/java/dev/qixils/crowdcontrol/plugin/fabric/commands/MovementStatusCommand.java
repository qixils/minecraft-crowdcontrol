package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.components.MovementStatusType;
import dev.qixils.crowdcontrol.common.components.MovementStatusValue;
import dev.qixils.crowdcontrol.common.util.ComparableUtil;
import dev.qixils.crowdcontrol.common.util.SemVer;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.CCTimedEffect;
import live.crowdcontrol.cc4j.websocket.data.CCTimedEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.DISABLE_JUMPING_DURATION;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.INVERT_CONTROLS_DURATION;

@Getter
public class MovementStatusCommand extends ModdedCommand implements CCTimedEffect {
	private final String effectName;
	private final String effectGroup;
	private final Duration defaultDuration;
	private final MovementStatusType type;
	private final MovementStatusValue value;
    private final SemVer minimumModVersion;
	private final Map<UUID, Set<UUID>> idMap = new HashMap<>();

	public MovementStatusCommand(ModdedCrowdControlPlugin plugin, String effectName, String effectGroup, Duration defaultDuration, MovementStatusType type, MovementStatusValue value, boolean clientOnly) {
		super(plugin);
		this.effectName = effectName;
		this.effectGroup = effectGroup;
		this.defaultDuration = defaultDuration;
		this.type = type;
		this.value = value;
		if (clientOnly)
			this.minimumModVersion = ComparableUtil.max(type.addedIn(), value.addedIn());
		else
			this.minimumModVersion = SemVer.ZERO;
	}

	public MovementStatusCommand(ModdedCrowdControlPlugin plugin, String effectName, Duration defaultDuration, MovementStatusType type, MovementStatusValue value, boolean clientOnly) {
		this(plugin, effectName, effectName, defaultDuration, type, value, clientOnly);
	}

	@Override
	public void execute(@NotNull Supplier<List<ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			List<ServerPlayer> players = playerSupplier.get();
			idMap.put(request.getRequestId(), players.stream().map(ServerPlayer::getUUID).collect(Collectors.toSet()));

			for (Player player : players)
				player.cc$setMovementStatus(type, value);

			return new CCTimedEffectResponse(request.getRequestId(), ResponseStatus.TIMED_BEGIN, request.getEffect().getDurationMillis());
		}));
	}

	@Override
	public void onEnd(@NotNull PublicEffectPayload request, @NotNull CCPlayer source) {
		plugin.toPlayerStream(idMap.remove(request.getRequestId())).forEach(player -> player.cc$setMovementStatus(type, MovementStatusValue.ALLOWED));
	}

	public static MovementStatusCommand disableJumping(ModdedCrowdControlPlugin plugin) {
		return new MovementStatusCommand(plugin, "disable_jumping", DISABLE_JUMPING_DURATION, MovementStatusType.JUMP, MovementStatusValue.DENIED, false);
	}

	public static MovementStatusCommand invertControls(ModdedCrowdControlPlugin plugin) {
		return new MovementStatusCommand(plugin, "invert_wasd", "walk", INVERT_CONTROLS_DURATION, MovementStatusType.WALK, MovementStatusValue.INVERTED, true);
	}

	public static MovementStatusCommand invertCamera(ModdedCrowdControlPlugin plugin) {
		return new MovementStatusCommand(plugin, "invert_look", "look", INVERT_CONTROLS_DURATION, MovementStatusType.LOOK, MovementStatusValue.INVERTED, true);
	}
}
