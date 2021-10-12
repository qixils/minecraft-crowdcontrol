package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.VoidCommand;
import dev.qixils.crowdcontrol.plugin.utils.TextUtil;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Getter
public class GamemodeCommand extends VoidCommand {
    private static final Duration DURATION = Duration.ofSeconds(30);
    private final GameMode gamemode;
    private final String displayName;
    private final String effectName;

    public GamemodeCommand(CrowdControlPlugin plugin, GameMode gamemode) {
        super(plugin);
        this.gamemode = gamemode;
        this.displayName = TextUtil.titleCase(gamemode) + " Mode";
        this.effectName = gamemode.name().toLowerCase(Locale.ENGLISH) + "_mode";
    }

    @Override
    public void voidExecute(@NotNull Request request) {
        // use fake request (w/ fake ID) to only allow 1 of any gamemode command to run at a time
        Request fakeRequest = new Request(request.getId(), "gamemode", request.getParameters(), request.getViewer(), request.getCost(), request.getType());
        List<Player> players = new ArrayList<>();
        new TimedEffect(Objects.requireNonNull(plugin.getCrowdControl(), "CC not initialized"),
                fakeRequest, DURATION,
                $ -> players.addAll(setGameMode(CrowdControlPlugin.getPlayers(), gamemode)),
                $ -> setGameMode(players, GameMode.SURVIVAL)).queue();
    }

    private List<Player> setGameMode(List<Player> players, GameMode gamemode) {
        for (Player player : players) {
            if (player.isValid())
                Bukkit.getScheduler().runTask(plugin, () -> player.setGameMode(gamemode));
        }
        return players;
    }
}
