package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.TimedCommand;
import dev.qixils.crowdcontrol.plugin.utils.PlayerListWrapper;
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
    public void voidExecute(@NotNull List<@NotNull Player> ignored, @NotNull Request request) {
        List<Player> players = new ArrayList<>();

        PlayerListWrapper wrapper = new PlayerListWrapper(request,
                curPlayers -> players.addAll(setGameMode(request, curPlayers, gamemode))
        );

        new TimedEffect(request, "gamemode", duration,
                $ -> CrowdControlPlugin.getPlayers(request).whenComplete(wrapper),
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
                Bukkit.getScheduler().runTask(plugin, () -> player.setGameMode(gamemode));
        }
        return players;
    }
}
