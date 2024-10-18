package dev.qixils.crowdcontrol.plugin.paper.commands;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import dev.qixils.crowdcontrol.common.components.MovementStatusType;
import dev.qixils.crowdcontrol.common.components.MovementStatusValue;
import dev.qixils.crowdcontrol.common.packets.MovementStatusPacketS2C;
import dev.qixils.crowdcontrol.common.util.ComparableUtil;
import dev.qixils.crowdcontrol.common.util.SemVer;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.paper.PaperCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.CCTimedEffect;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.CCTimedEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.DISABLE_JUMPING_DURATION;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.INVERT_CONTROLS_DURATION;
import static dev.qixils.crowdcontrol.plugin.paper.utils.PaperUtil.toPlayers;

@Getter
public class MovementStatusCommand extends PaperCommand implements CCTimedEffect {
	private final Map<UUID, List<UUID>> idMap = new HashMap<>();
	private final String effectName;
	private final String effectGroup;
	private final Duration defaultDuration;
	private final MovementStatusType type;
	private final MovementStatusValue value;
    private final SemVer minimumModVersion;

	public MovementStatusCommand(PaperCrowdControlPlugin plugin, String effectName, String effectGroup, Duration defaultDuration, MovementStatusType type, MovementStatusValue value, boolean clientOnly) {
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

	public MovementStatusCommand(PaperCrowdControlPlugin plugin, String effectName, Duration defaultDuration, MovementStatusType type, MovementStatusValue value, boolean clientOnly) {
		this(plugin, effectName, effectName, defaultDuration, type, value, clientOnly);
	}

	private static NamespacedKey key(@NotNull MovementStatusType type) {
		return new NamespacedKey("crowdcontrol", "movement-status-" + type.name().toLowerCase().replace('_', '-'));
	}

	@NotNull
	public static MovementStatusValue getValue(@NotNull Player player, @NotNull MovementStatusType type) {
		var pdc = player.getPersistentDataContainer();
		var key = key(type);

		return pdc.getOrDefault(
			key,
			PaperCrowdControlPlugin.MOVEMENT_STATUS_VALUE_TYPE,
			MovementStatusValue.ALLOWED
		);
	}

	public static void setValue(@NotNull PaperCrowdControlPlugin plugin, @NotNull Player player, @NotNull MovementStatusType type, @NotNull MovementStatusValue value) {
		var pdc = player.getPersistentDataContainer();
		var key = key(type);

		if (value == MovementStatusValue.ALLOWED)
			pdc.remove(key);
		else
			pdc.set(
				key,
				PaperCrowdControlPlugin.MOVEMENT_STATUS_VALUE_TYPE,
				value
			);

		plugin.getPluginChannel().sendMessage(player, new MovementStatusPacketS2C(type, value));
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull Player>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(() -> {
			if (isActive(ccPlayer, effectGroup))
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Unable to stack movement statuses");

			List<Player> players = playerSupplier.get();
			if (minimumModVersion.isGreaterThan(SemVer.ZERO))
				players.removeIf(player -> plugin.getModVersion(player).orElse(SemVer.ZERO).isLessThan(minimumModVersion));

			idMap.put(request.getRequestId(), players.stream().map(Player::getUniqueId).toList());

			if (players.isEmpty())
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "No targetable players online");

			for (Player player : players)
				setValue(plugin, player, type, value);

			return new CCTimedEffectResponse(request.getRequestId(), ResponseStatus.TIMED_BEGIN, request.getEffect().getDuration() * 1000L);
		}));
	}

	@Override
	public void onEnd(@NotNull PublicEffectPayload request, @NotNull CCPlayer source) {
		for (Player player : toPlayers(idMap.remove(request.getRequestId())))
			setValue(plugin, player, type, MovementStatusValue.ALLOWED);
	}

	public static MovementStatusCommand disableJumping(PaperCrowdControlPlugin plugin) {
		return new MovementStatusCommand(plugin, "disable_jumping", DISABLE_JUMPING_DURATION, MovementStatusType.JUMP, MovementStatusValue.DENIED, false);
	}

	public static MovementStatusCommand invertControls(PaperCrowdControlPlugin plugin) {
		return new MovementStatusCommand(plugin, "invert_wasd", "walk", INVERT_CONTROLS_DURATION, MovementStatusType.WALK, MovementStatusValue.INVERTED, true);
	}

	public static MovementStatusCommand invertCamera(PaperCrowdControlPlugin plugin) {
		return new MovementStatusCommand(plugin, "invert_look", "look", INVERT_CONTROLS_DURATION, MovementStatusType.LOOK, MovementStatusValue.INVERTED, true);
	}

	public static final class Manager implements Listener {
		@EventHandler
		public void onJump(PlayerJumpEvent event) {
			Player player = event.getPlayer();
			boolean cantJump = getValue(player, MovementStatusType.JUMP) == MovementStatusValue.DENIED;
			boolean cantWalk = getValue(player, MovementStatusType.WALK) == MovementStatusValue.DENIED;
			if (cantJump || cantWalk) {
				event.setCancelled(true);
			}
		}
	}
}
