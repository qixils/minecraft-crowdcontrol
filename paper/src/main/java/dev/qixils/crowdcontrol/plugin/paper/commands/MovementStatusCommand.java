package dev.qixils.crowdcontrol.plugin.paper.commands;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.common.components.MovementStatusType;
import dev.qixils.crowdcontrol.common.components.MovementStatusValue;
import dev.qixils.crowdcontrol.common.packets.MovementStatusPacketS2C;
import dev.qixils.crowdcontrol.common.util.ComparableUtil;
import dev.qixils.crowdcontrol.common.util.SemVer;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.TimedVoidCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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

	public MovementStatusCommand(PaperCrowdControlPlugin plugin, String effectName, String effectGroup, Duration defaultDuration, MovementStatusType type, MovementStatusValue value, boolean clientOnly) {
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
	public void voidExecute(@NotNull List<@NotNull Player> ignored, @NotNull Request request) {
		AtomicReference<List<Player>> atomicPlayers = new AtomicReference<>();
		new TimedEffect.Builder()
			.request(request)
			.effectGroup(effectGroup)
			.duration(getDuration(request))
			.startCallback($ -> {
				List<Player> players = plugin.getPlayers(request);
				if (clientOnly)
					players.removeIf(player -> plugin.getModVersion(player).orElse(SemVer.ZERO).isLessThan(minimumModVersion));
				atomicPlayers.set(players);

				if (players.isEmpty())
					return request.buildResponse()
						.type(Response.ResultType.FAILURE)
						.message("No targetable players online");

				for (Player player : players)
					setValue(plugin, player, type, value);
				playerAnnounce(players, request);

				return null; // success
			})
			.completionCallback($ -> {
				for (Player player : atomicPlayers.get())
					setValue(plugin, player, type, MovementStatusValue.ALLOWED);
			})
			.build().queue();
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
