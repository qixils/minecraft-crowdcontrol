package io.github.lexikiq.crowdcontrol.commands;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import io.github.lexikiq.crowdcontrol.ChatCommand;
import io.github.lexikiq.crowdcontrol.CrowdControl;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class VeinCommand extends ChatCommand {
    public VeinCommand(CrowdControl plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String getCommand() {
        return "vein";
    }

    @Override
    public int getCooldownSeconds() {
        return 60*15;
    }

    @Override
    public void execute(ChannelMessageEvent event, Collection<? extends Player> players) {
        super.execute(event, players);
        // TODO: spawns a vein of ores (or silverfish blocks) around players
    }
}
