package dev.qixils.crowdcontrol.plugin;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Random;

public abstract class Command implements dev.qixils.crowdcontrol.common.Command<Player> {
    protected static final Random random = new Random();
    @Getter
    protected final BukkitCrowdControlPlugin plugin;

    protected Command(@NotNull BukkitCrowdControlPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }
}
