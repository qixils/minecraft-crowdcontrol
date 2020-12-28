package io.github.lexikiq.crowdcontrol;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Random;

public abstract class ChatCommand {
    public static final Random rand = new Random();

    protected LocalDateTime used = null;
    public abstract int getCooldownSeconds();
    public boolean canUse() {
        return used == null || used.plusSeconds(getCooldownSeconds()).isBefore(LocalDateTime.now());
    }

    public abstract @NotNull String getCommand();

    public void execute(ChannelMessageEvent event, Collection<? extends Player> players){
        used = LocalDateTime.now();
    }

    protected final CrowdControl plugin;
    public ChatCommand(CrowdControl plugin) {
        this.plugin = plugin;
    }

}
