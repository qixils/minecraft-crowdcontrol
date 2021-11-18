package dev.qixils.crowdcontrol.plugin;

import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A command which handles the {@link Response}(s) on its own.
 */
public abstract class VoidCommand extends Command {
    public VoidCommand(@NotNull CrowdControlPlugin plugin) {
        super(plugin);
    }

    @Override
    protected final @NotNull CompletableFuture<Response.@NotNull Builder> execute(@NotNull List<@NotNull Player> players, @NotNull Request request) {
        voidExecute(players, request);
        return CompletableFuture.completedFuture(null);
    }

    protected abstract void voidExecute(@NotNull List<@NotNull Player> players, @NotNull Request request);
}
