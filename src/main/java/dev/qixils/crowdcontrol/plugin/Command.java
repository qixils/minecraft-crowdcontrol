package dev.qixils.crowdcontrol.plugin;

import dev.qixils.crowdcontrol.exceptions.NoApplicableTarget;
import dev.qixils.crowdcontrol.plugin.utils.PlayerListWrapper;
import dev.qixils.crowdcontrol.plugin.utils.TextBuilder;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Request.Target;
import dev.qixils.crowdcontrol.socket.Response;
import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.CheckReturnValue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
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

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected final boolean isGlobalCommandUsable(@Nullable List<Player> players, @NotNull Request request) {
        if (CrowdControlPlugin.isGlobal(request))
            return true;

        Collection<String> hosts = plugin.getHosts();
        if (hosts.isEmpty())
            return false;

        for (Target target : request.getTargets()) {
            if (hosts.contains(String.valueOf(target.getId())))
                return true;
            if (hosts.contains(target.getName().toLowerCase(Locale.ENGLISH)))
                return true;
        }

        if (players == null)
            players = CrowdControlPlugin.getPlayers(request).join();

        for (Player player : players) {
            String uuidStr = player.getUniqueId().toString().toLowerCase(Locale.ENGLISH);
            if (hosts.contains(uuidStr) || hosts.contains(uuidStr.replace("-", "")))
                return true;
            if (hosts.contains(player.getName().toLowerCase(Locale.ENGLISH)))
                return true;
        }

        return false;
    }

    @Deprecated
    protected final void announce(final Request request) {
        if (!plugin.announceEffects()) return;
        PlayerListWrapper wrapper = new PlayerListWrapper(request, players -> announce(players, request));
        CrowdControlPlugin.getPlayers(request).whenComplete(wrapper);
    }

    protected final void announce(final Collection<? extends Audience> audiences, final Request request) {
        if (!plugin.announceEffects()) return;
        announce(Audience.audience(audiences), request);
    }

    protected final void announce(final Audience audience, final Request request) {
        if (!plugin.announceEffects()) return;
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
