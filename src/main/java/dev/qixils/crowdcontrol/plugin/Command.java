package dev.qixils.crowdcontrol.plugin;

import dev.qixils.crowdcontrol.exceptions.NoApplicableTarget;
import dev.qixils.crowdcontrol.plugin.utils.TextBuilder;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.CheckReturnValue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public abstract class Command {
    public static final Random rand = new Random();

    @NotNull
    @CheckReturnValue
    protected abstract CompletableFuture<Response.Builder> execute(@NotNull List<@NotNull Player> players, @NotNull Request request);

    @NotNull
    @CheckReturnValue
    public abstract String getEffectName();

    @NotNull
    @CheckReturnValue
    public abstract String getDisplayName();

    public final void executeAndNotify(@NotNull Request request) {
        List<Player> players = CrowdControlPlugin.getPlayers(request).join();

        // ensure targets are online / available
        if (players.isEmpty())
            throw new NoApplicableTarget();

        execute(new ArrayList<>(players), request).thenAccept(builder -> {
            if (builder == null) return;

            Response response = builder.build();
            response.send();

            if (response.getResultType() == Response.ResultType.SUCCESS)
                announce(players, request);
        });
    }

    @Deprecated
    public final void announce(final Request request) {
        CrowdControlPlugin.getPlayers(request).thenAccept(players -> announce(players, request));
    }

    protected final void announce(final Collection<? extends Audience> audiences, final Request request) {
        announce(Audience.audience(audiences), request);
    }

    protected final void announce(final Audience audience, final Request request) {
        audience.sendMessage(new TextBuilder()
                .next(request.getViewer(), CrowdControlPlugin.USER_COLOR)
                .next(" used command ")
                .next(getProcessedDisplayName(), CrowdControlPlugin.CMD_COLOR)
        );
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
