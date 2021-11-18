package dev.qixils.crowdcontrol.plugin;

import dev.qixils.crowdcontrol.plugin.utils.TextBuilder;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import javax.annotation.CheckReturnValue;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public abstract class Command {
    public static final Random rand = new Random();

    @NotNull
    @CheckReturnValue
    public abstract CompletableFuture<Response.Builder> execute(@NotNull Request request);

    @NotNull
    @CheckReturnValue
    public abstract String getEffectName();

    @NotNull
    @CheckReturnValue
    public abstract String getDisplayName();

    public final void executeAndNotify(@NotNull Request request) {
        execute(request).thenAccept(builder -> {
            if (builder == null) return;

            Response response = builder.build();
            response.send();

            if (response.getResultType() == Response.ResultType.SUCCESS)
                announce(request);
        });
    }

    public final void announce(final Request viewer) {
        // TODO only announce to targets
        announce(viewer.getViewer());
    }

    @Deprecated
    public final void announce(final String viewer) {
        Bukkit.getServer().sendMessage(new TextBuilder()
                .next(viewer, CrowdControlPlugin.USER_COLOR)
                .next(" used command ")
                .next(getProcessedDisplayName(), CrowdControlPlugin.CMD_COLOR));
    }

    @NotNull
    @CheckReturnValue
    protected String getProcessedDisplayName() {
        return getDisplayName();
    }

    protected final CrowdControlPlugin plugin;

    @CheckReturnValue
    public Command(@NotNull CrowdControlPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

}
