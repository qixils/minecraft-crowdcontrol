package dev.qixils.crowdcontrol.plugin;

import dev.qixils.crowdcontrol.socket.Response;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * A command which handles the {@link Response}(s) on its own.
 */
public abstract class VoidCommand extends Command implements dev.qixils.crowdcontrol.common.VoidCommand<Player> {
    public VoidCommand(@NotNull BukkitCrowdControlPlugin plugin) {
        super(plugin);
    }
}
