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
        Request fakeRequest = new Request(request.getId(), "gamemode", new Object[0], request.getViewer(), request.getCost(), request.getType());
        new TimedEffect(Objects.requireNonNull(plugin.getCrowdControl(), "CC cannot be null"),
                fakeRequest, DURATION,
                $ -> setGameMode(gamemode),
                $ -> setGameMode(GameMode.SURVIVAL)).queue();
    }

    private void setGameMode(GameMode gamemode) {
        for (Player player : CrowdControlPlugin.getPlayers())
            Bukkit.getScheduler().runTask(plugin, () -> player.setGameMode(gamemode));
    }
}
