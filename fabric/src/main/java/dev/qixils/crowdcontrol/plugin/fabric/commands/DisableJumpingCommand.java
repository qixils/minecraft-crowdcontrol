package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.common.EventListener;
import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.TimedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.event.Jump;
import dev.qixils.crowdcontrol.plugin.fabric.event.Listener;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@EventListener
public class DisableJumpingCommand extends TimedCommand {
	private final Map<UUID, Integer> jumpsBlockedAt = new HashMap<>();
	private final String effectName = "disable_jumping";

	public DisableJumpingCommand(FabricCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public @NotNull Duration getDuration() {
		return CommandConstants.DISABLE_JUMPING_DURATION;
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull ServerPlayer> ignored, @NotNull Request request) {
		new TimedEffect.Builder().request(request)
				.duration(CommandConstants.DISABLE_JUMPING_DURATION)
				.startCallback($ -> {
					List<ServerPlayer> players = plugin.getPlayers(request);
					if (players.isEmpty())
						return request.buildResponse()
								.type(ResultType.FAILURE)
								.message("No players online");

					int tick = plugin.server().getTickCount();
					for (Player player : players)
						jumpsBlockedAt.put(player.getUUID(), tick);
					playerAnnounce(players, request);

					return null; // success
				}).build().queue();
	}

	@Listener
	public void onJumpEvent(Jump event) {
		UUID uuid = event.player().getUUID();
		if (!jumpsBlockedAt.containsKey(uuid)) return;
		int blockedAt = jumpsBlockedAt.get(uuid);
		if ((blockedAt + CommandConstants.DISABLE_JUMPING_TICKS) >= plugin.server().getTickCount())
			event.cancelled(true);
		else
			jumpsBlockedAt.remove(uuid, blockedAt);
	}
}
