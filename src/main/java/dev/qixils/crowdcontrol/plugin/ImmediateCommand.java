package dev.qixils.crowdcontrol.plugin;

import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A command whose result is available immediately
 */
public abstract class ImmediateCommand extends Command {
    public ImmediateCommand(CrowdControlPlugin plugin) {
        super(plugin);
    }

    @Override
    protected final @NotNull CompletableFuture<Response.@NotNull Builder> execute(@NotNull List<@NotNull Player> players, @NotNull Request request) {
        return CompletableFuture.completedFuture(executeImmediately(players, request));
    }

    protected abstract Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request);
}
