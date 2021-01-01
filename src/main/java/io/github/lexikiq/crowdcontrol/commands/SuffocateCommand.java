package io.github.lexikiq.crowdcontrol.commands;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import io.github.lexikiq.crowdcontrol.ChatCommand;
import io.github.lexikiq.crowdcontrol.CrowdControl;
import io.github.lexikiq.crowdcontrol.utils.BlockUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SuffocateCommand extends ChatCommand {
    protected static final Material SET_MATERIAL = Material.GLASS;
    protected static final double RADIUS = 1.5;

    public SuffocateCommand(CrowdControl plugin) {
        super(plugin);
    }

    @Override
    public int getCooldownSeconds() {
        return (int) (60*7.5);
    }

    @Override
    public @NotNull String getCommand() {
        return "freeze";
    }

    @Override
    public boolean execute(ChannelMessageEvent event, List<Player> players, String... args) {
        for (Player player : players) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Location base = player.getLocation();
                    for (double x = -RADIUS; x <= RADIUS; ++x) {
                        for (int y = -1; y <= 2; ++y) {
                            for (double z = -RADIUS; z <= RADIUS; ++z) {
                                Location location = base.clone().add(x, y, z);
                                Block block = location.getBlock();
                                if (BlockUtil.AIR_BLOCKS.contains(block.getType())) {
                                    location.getBlock().setType(SET_MATERIAL);
                                }
                            }
                        }
                    }
                }
            }.runTask(plugin);
        }
        return true;
    }
}
