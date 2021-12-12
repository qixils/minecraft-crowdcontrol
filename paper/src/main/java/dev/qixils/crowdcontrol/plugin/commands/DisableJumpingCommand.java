package dev.qixils.crowdcontrol.plugin.commands;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.TimedCommand;
import dev.qixils.crowdcontrol.socket.Request;
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
    private static final Duration DURATION = Duration.ofSeconds(10);
    private static final int JUMP_BLOCK_DURATION = (int) (DURATION.toSeconds() * 20);

    private final Map<UUID, Integer> jumpsBlockedAt = new HashMap<>();
    private final String effectName = "disable_jumping";
    private final String displayName = "Disable Jumping";

    public DisableJumpingCommand(CrowdControlPlugin plugin) {
        super(plugin);
    }

    @Override
    public @NotNull Duration getDuration() {
        return DURATION;
    }

    @Override
    public void voidExecute(@NotNull List<@NotNull Player> ignored, @NotNull Request request) {
        new TimedEffect(request, DURATION,
                $ -> {
                    List<Player> players = plugin.getPlayers(request);
                    int tick = Bukkit.getCurrentTick();
                    for (Player player : players)
                        jumpsBlockedAt.put(player.getUniqueId(), tick);
                    announce(players, request);
                },
                null
        ).queue();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onJumpEvent(PlayerJumpEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (!jumpsBlockedAt.containsKey(uuid)) return;
        int blockedAt = jumpsBlockedAt.get(uuid);
        if ((blockedAt + JUMP_BLOCK_DURATION) >= Bukkit.getCurrentTick())
            event.setCancelled(true);
        else
            jumpsBlockedAt.remove(uuid, blockedAt);
    }
}
