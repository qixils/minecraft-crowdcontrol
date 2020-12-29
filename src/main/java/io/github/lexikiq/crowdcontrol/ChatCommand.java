package io.github.lexikiq.crowdcontrol;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Random;

public abstract class ChatCommand {
    public static final Random rand = new Random();

    protected LocalDateTime used = LocalDateTime.MIN;
    public abstract int getCooldownSeconds();
    public ClassCooldowns getClassCooldown() {
        return null;
    }
    public boolean canUse() {
        return used.plusSeconds(getCooldownSeconds()).isBefore(LocalDateTime.now());
    }
    public void setCooldown() {
        used = LocalDateTime.now();
    }

    public abstract @NotNull String getCommand();

    public abstract boolean execute(ChannelMessageEvent event, Collection<? extends Player> players, String... args);

    protected final CrowdControl plugin;
    public ChatCommand(CrowdControl plugin) {
        this.plugin = plugin;
    }

}
