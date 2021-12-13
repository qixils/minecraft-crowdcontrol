package dev.qixils.crowdcontrol.plugin;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Random;

public abstract class Command implements dev.qixils.crowdcontrol.common.Command<Player> {
    public static final Random rand = new Random();
    protected final BukkitCrowdControlPlugin plugin;

    public Command(@NotNull BukkitCrowdControlPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    @NotNull
    @Override
    public BukkitCrowdControlPlugin getPlugin() {
        return plugin;
    }
}
