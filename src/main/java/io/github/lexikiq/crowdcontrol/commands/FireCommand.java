package io.github.lexikiq.crowdcontrol.commands;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import io.github.lexikiq.crowdcontrol.ChatCommand;
import io.github.lexikiq.crowdcontrol.CrowdControl;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class FireCommand extends ChatCommand {
    public FireCommand(CrowdControl plugin) {
        super(plugin);
    }

    @Override
    public int getCooldownSeconds() {
        return 45;
    }

    @Override
    public @NotNull String getCommand() {
        return "fire";
    }

    @Override
    public boolean execute(ChannelMessageEvent event, Collection<? extends Player> players) {
        boolean didSomething = false;
        for (Player player : players) {
            Block block = player.getLocation().getBlock();
            if (block.getType() == Material.AIR) {
                didSomething = true;
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        block.setType(Material.FIRE);
                    }
                }.runTask(plugin);
            }
        }
        return didSomething;
    }
}
