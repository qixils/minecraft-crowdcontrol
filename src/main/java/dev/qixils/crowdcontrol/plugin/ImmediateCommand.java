package dev.qixils.crowdcontrol.plugin;

import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * A command whose result is available immediately
 */
public abstract class ImmediateCommand extends Command {
    public ImmediateCommand(CrowdControlPlugin plugin) {
        super(plugin);
    }

    @Override
    public final @NotNull CompletableFuture<Response.@NotNull Builder> execute(@NotNull Request request) {
        return CompletableFuture.completedFuture(executeImmediately(request));
    }

    public abstract Response.@NotNull Builder executeImmediately(@NotNull Request request);
}
