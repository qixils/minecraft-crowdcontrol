package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.TimedCommand;
import dev.qixils.crowdcontrol.plugin.utils.TextUtil;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Getter
public class GamemodeCommand extends TimedCommand {
    private final Duration duration;
    private final GameMode gamemode;
    private final String displayName;
    private final String effectName;

    public GamemodeCommand(CrowdControlPlugin plugin, GameMode gamemode, long seconds) {
        super(plugin);
        this.duration = Duration.ofSeconds(seconds);
        this.gamemode = gamemode;
        this.displayName = TextUtil.titleCase(gamemode) + " Mode";
        this.effectName = gamemode.name().toLowerCase(Locale.ENGLISH) + "_mode";
    }

    @Override
    public void voidExecute(@NotNull List<@NotNull Player> originalPlayers, @NotNull Request request) {
        // use fake request (w/ fake ID) to only allow 1 of any gamemode command to run at a time
        List<Player> players = new ArrayList<>();
        new TimedEffect(request, "gamemode", duration,
                $ -> players.addAll(setGameMode(request, originalPlayers, gamemode)),
                $ -> setGameMode(null, players, GameMode.SURVIVAL)).queue();
    }

    private List<Player> setGameMode(@Nullable Request request,
                                     @NotNull List<@NotNull Player> players,
                                     @NotNull GameMode gamemode) {
        if (players.isEmpty()) return players;
        if (request != null)
            announce(request);
        for (Player player : players) {
            if (player.isValid())
                Bukkit.getScheduler().runTask(plugin, () -> player.setGameMode(gamemode));
        }
        return players;
    }
}
