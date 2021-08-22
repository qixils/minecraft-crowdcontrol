package dev.qixils.crowdcontrol.plugin;

import dev.qixils.crowdcontrol.plugin.utils.TextBuilder;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Random;

public abstract class Command {
    public static final Random rand = new Random();

    public abstract Response.@NotNull Result execute(@NotNull Request request);
    @NotNull
    public abstract String getEffectName();

    @NotNull
    public String getDisplayName() {
        StringBuilder sb = new StringBuilder();
        char[] chars = getClass().getSimpleName().replace("Command", "").toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char chr = chars[i];
            if (i > 0 && Character.isUpperCase(chr))
                sb.append(' ');
            sb.append(chr);
        }
        return sb.toString();
    }

    public final Response.@NotNull Result executeAndNotify(@NotNull Request request) {
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        if (players.isEmpty())
            return new Response.Result(Response.ResultType.UNAVAILABLE, "No players online");
        if (players.stream().allMatch(Entity::isDead))
            return new Response.Result(Response.ResultType.RETRY, "All players are dead");

        Response.Result result = execute(request);
        if (result.getType() == Response.ResultType.SUCCESS)
            Bukkit.getServer().sendMessage(new TextBuilder()
                .next(request.getViewer(), CrowdControlPlugin.USER_COLOR)
                .next(" used command ")
                .next(getDisplayName(), CrowdControlPlugin.CMD_COLOR));
        return result;
    }

    protected final CrowdControlPlugin plugin;
    public Command(CrowdControlPlugin plugin) {
        this.plugin = plugin;
    }

}
