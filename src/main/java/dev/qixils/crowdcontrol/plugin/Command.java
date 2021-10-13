package dev.qixils.crowdcontrol.plugin;

import dev.qixils.crowdcontrol.CrowdControl;
import dev.qixils.crowdcontrol.plugin.utils.TextBuilder;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public abstract class Command {
    public static final Random rand = new Random();

    public abstract @NotNull CompletableFuture<Response.Builder> execute(@NotNull Request request);
    @NotNull
    public abstract String getEffectName();

    @NotNull
    public abstract String getDisplayName();

    public final void executeAndNotify(@NotNull Request request) {
        execute(request).thenAccept(builder -> {
            if (builder == null) return;

            Response response = builder.id(request.getId()).build();

            CrowdControl cc = plugin.getCrowdControl();
            if (cc != null) cc.dispatchResponse(response);

            if (response.getResultType() == Response.ResultType.SUCCESS)
                announce(request);
        });
    }

    public final void announce(final Request viewer) {
        announce(viewer.getViewer());
    }

    public final void announce(final String viewer) {
        Bukkit.getServer().sendMessage(new TextBuilder()
                .next(viewer, CrowdControlPlugin.USER_COLOR)
                .next(" used command ")
                .next(getDisplayName(), CrowdControlPlugin.CMD_COLOR));
    }

    protected final CrowdControlPlugin plugin;
    public Command(@NotNull CrowdControlPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

}
