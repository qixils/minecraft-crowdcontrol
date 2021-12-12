package dev.qixils.crowdcontrol.plugin;

import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A command whose result is available immediately
 */
public abstract class ImmediateCommand extends Command implements dev.qixils.crowdcontrol.common.ImmediateCommand<Player> {
    public ImmediateCommand(CrowdControlPlugin plugin) {
        super(plugin);
    }

    public abstract Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request);
}
