package dev.qixils.crowdcontrol.plugin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import dev.qixils.crowdcontrol.common.PlayerMapper;
import dev.qixils.crowdcontrol.plugin.commands.GamemodeCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Request.Target;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.CheckReturnValue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public final class BukkitPlayerMapper implements Listener, PlayerMapper<Player> {
    private final CrowdControlPlugin plugin;
    final Multimap<String, UUID> twitchToUserMap =
            Multimaps.synchronizedSetMultimap(HashMultimap.create(1, 1));

    @Contract(value = "_ -> param1", mutates = "param1")
    private @NotNull List<@NotNull Player> filter(@NotNull List<Player> players) {
        players.removeIf(player -> player == null
                || !player.isValid()
                || player.isDead()
                || (player.getGameMode() == GameMode.SPECTATOR && !GamemodeCommand.isEffectActive(plugin, player))
        );
        return players;
    }

    @CheckReturnValue
    @NotNull
    public List<@NotNull Player> getAllPlayers() {
        return filter(new ArrayList<>(Bukkit.getOnlinePlayers()));
    }

    @CheckReturnValue
    @NotNull
    public List<@NotNull Player> getPlayers(final @NotNull Request request) {
        if (plugin.isGlobal(request))
            return getAllPlayers();

        List<Player> players = new ArrayList<>(request.getTargets().length);
        for (Target target : request.getTargets()) {
            for (UUID uuid : twitchToUserMap.get(target.getName()))
                players.add(Bukkit.getPlayer(uuid));
        }

        return filter(players);
    }

    @Override
    public boolean linkPlayer(@NotNull UUID uuid, @NotNull String twitchUsername) {
        return twitchToUserMap.put(twitchUsername, uuid);
    }

    @Override
    public boolean unlinkPlayer(@NotNull UUID uuid, @NotNull String twitchUsername) {
        return twitchToUserMap.remove(twitchUsername, uuid);
    }

    @Override
    public @NotNull Collection<@NotNull String> getLinkedAccounts(@NotNull UUID uuid) {
        return Multimaps.invertFrom(twitchToUserMap, HashMultimap.create(twitchToUserMap.size(), 1)).get(uuid);
    }

    @Override
    public @NotNull Collection<@NotNull UUID> getLinkedPlayers(@NotNull String twitchUsername) {
        return twitchToUserMap.get(twitchUsername);
    }
}
