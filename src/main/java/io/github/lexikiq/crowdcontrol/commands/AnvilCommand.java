package io.github.lexikiq.crowdcontrol.commands;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import io.github.lexikiq.crowdcontrol.ChatCommand;
import io.github.lexikiq.crowdcontrol.CrowdControl;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AnvilCommand extends ChatCommand {
    public AnvilCommand(CrowdControl plugin) {
        super(plugin);
    }

    @Override
    public int getCooldownSeconds() {
        return (int) (60*2.5);
    }

    @Override
    public @NotNull String getCommand() {
        return "anvil";
    }

    @Override
    public boolean execute(ChannelMessageEvent event, List<Player> players, String... args) {
        // input parsing
        if (args.length < 1) {
            return false;
        }
        int y;
        try {
            y = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            return false;
        }
        if (y < 1) {
            return false;
        }

        // spawn the damn anvil
        for (Player player : players) {
            Location destination = player.getEyeLocation();
            destination.setY(Math.min(destination.getY()+y, player.getWorld().getMaxHeight()-1));
            Block block = destination.getBlock();
            if (CrowdControl.AIR_BLOCKS.contains(block.getType())) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        block.setType(Material.ANVIL, true);
                    }
                }.runTask(plugin);
            }
        }
        return true;
    }
}
