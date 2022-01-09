package dev.qixils.crowdcontrol.plugin.commands;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.common.CommandConstants;
import dev.qixils.crowdcontrol.plugin.BukkitCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.TimedCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public class DisableJumpingCommand extends TimedCommand implements Listener {
    private final Map<UUID, Integer> jumpsBlockedAt = new HashMap<>();
    private final String effectName = "disable_jumping";
    private final String displayName = "Disable Jumping";

    public DisableJumpingCommand(BukkitCrowdControlPlugin plugin) {
        super(plugin);
    }

    @Override
    public @NotNull Duration getDuration() {
		return CommandConstants.DISABLE_JUMPING_DURATION;
    }

    @Override
    public void voidExecute(@NotNull List<@NotNull Player> ignored, @NotNull Request request) {
		new TimedEffect.Builder().request(request)
				.duration(CommandConstants.DISABLE_JUMPING_DURATION)
				.startCallback($ -> {
					List<Player> players = plugin.getPlayers(request);
					if (players.isEmpty())
						return request.buildResponse()
								.type(ResultType.FAILURE)
								.message("No players online");

					int tick = Bukkit.getCurrentTick();
					for (Player player : players)
						jumpsBlockedAt.put(player.getUniqueId(), tick);
					announce(players, request);

					return null; // success
				}).build().queue();
	}

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onJumpEvent(PlayerJumpEvent event) {
		UUID uuid = event.getPlayer().getUniqueId();
		if (!jumpsBlockedAt.containsKey(uuid)) return;
		int blockedAt = jumpsBlockedAt.get(uuid);
		if ((blockedAt + CommandConstants.DISABLE_JUMPING_TICKS) >= Bukkit.getCurrentTick())
			event.setCancelled(true);
		else
			jumpsBlockedAt.remove(uuid, blockedAt);
	}
}
