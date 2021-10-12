package dev.qixils.crowdcontrol.plugin;

import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * A command which handles the {@link Response}(s) on its own.
 */
public abstract class VoidCommand extends Command {
    public VoidCommand(@NotNull CrowdControlPlugin plugin) {
        super(plugin);
    }

    @Override
    public final @NotNull CompletableFuture<Response.@NotNull Builder> execute(@NotNull Request request) {
        voidExecute(request);
        return CompletableFuture.completedFuture(null);
    }

    public abstract void voidExecute(@NotNull Request request);
}
