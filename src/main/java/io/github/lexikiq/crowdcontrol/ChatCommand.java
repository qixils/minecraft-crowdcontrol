package io.github.lexikiq.crowdcontrol;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

public abstract class ChatCommand {
    public static final Random rand = new Random();

    protected LocalDateTime used = LocalDateTime.MIN;
    public abstract int getCooldownSeconds();
    public ClassCooldowns getClassCooldown() {
        return null;
    }
    public boolean canUse() {
        return refreshesAt().isBefore(LocalDateTime.now());
    }
    public void setCooldown() {
        used = LocalDateTime.now();
    }
    public LocalDateTime refreshesAt() {return used.plusSeconds(getCooldownSeconds());}

    public abstract @NotNull String getCommand();

    public abstract boolean execute(String authorName, List<Player> players, String... args);

    protected final CrowdControlPlugin plugin;
    public ChatCommand(CrowdControlPlugin plugin) {
        this.plugin = plugin;
    }

}
