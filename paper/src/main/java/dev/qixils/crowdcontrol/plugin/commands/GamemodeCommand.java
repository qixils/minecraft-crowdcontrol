package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.common.util.TextUtil;
import dev.qixils.crowdcontrol.plugin.BukkitCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.TimedCommand;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Getter
public class GamemodeCommand extends TimedCommand implements Listener {
    private final Duration duration;
    private final GameMode gamemode;
    private final String displayName;
    private final String effectName;
    private final NamespacedKey gamemodeKey;

    public GamemodeCommand(BukkitCrowdControlPlugin plugin, GameMode gamemode, long seconds) {
        super(plugin);
        this.gamemodeKey = getGamemodeKey(plugin);
        this.duration = Duration.ofSeconds(seconds);
        this.gamemode = gamemode;
        this.displayName = TextUtil.titleCase(gamemode) + " Mode";
        this.effectName = gamemode.name().toLowerCase(Locale.ENGLISH) + "_mode";
    }

    private static NamespacedKey getGamemodeKey(Plugin plugin) {
        return new NamespacedKey(plugin, "is_gamemode_active");
    }

    private static boolean isEffectActive(NamespacedKey key, Entity player) {
        return player.getPersistentDataContainer().getOrDefault(key, BukkitCrowdControlPlugin.BOOLEAN, false);
    }

    public static boolean isEffectActive(Plugin plugin, Entity player) {
        if (plugin == null) return false;
        return isEffectActive(getGamemodeKey(plugin), player);
    }

    @Override
    public void voidExecute(@NotNull List<@NotNull Player> ignored, @NotNull Request request) {
        List<Player> players = new ArrayList<>();

        new TimedEffect(request, "gamemode", duration,
                $ -> {
                    List<Player> curPlayers = plugin.getPlayers(request);
                    players.addAll(setGameMode(request, curPlayers, gamemode));
                },
                $ -> setGameMode(null, players, GameMode.SURVIVAL)).queue();
    }

    private List<Player> setGameMode(@Nullable Request request,
                                     @NotNull List<@NotNull Player> players,
                                     @NotNull GameMode gamemode) {
        if (players.isEmpty()) return players;
        if (request != null)
            announce(players, request);
        for (Player player : players) {
            if (player.isValid())
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.setGameMode(gamemode);
                    player.getPersistentDataContainer().set(gamemodeKey, BukkitCrowdControlPlugin.BOOLEAN, request != null);
                });
        }
        return players;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!isEffectActive(gamemodeKey, player)) return;
        player.getPersistentDataContainer().set(gamemodeKey, BukkitCrowdControlPlugin.BOOLEAN, false);
        player.setGameMode(GameMode.SURVIVAL);
    }
}
