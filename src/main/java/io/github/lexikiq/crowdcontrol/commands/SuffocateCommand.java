package io.github.lexikiq.crowdcontrol.commands;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.google.common.collect.ImmutableSet;
import io.github.lexikiq.crowdcontrol.ChatCommand;
import io.github.lexikiq.crowdcontrol.CrowdControl;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class SuffocateCommand extends ChatCommand {
    protected static final Material STONE_MATERIAL = Material.STONE;
    protected static final Set<Material> REPLACE = ImmutableSet.of(Material.AIR, Material.CAVE_AIR, Material.VOID_AIR);
    protected static final int RADIUS = 1;

    public SuffocateCommand(CrowdControl plugin) {
        super(plugin);
    }

    @Override
    public int getCooldownSeconds() {
        return (int) (60*7.5);
    }

    @Override
    public @NotNull String getCommand() {
        return "suffocate";
    }

    @Override
    public boolean execute(ChannelMessageEvent event, List<Player> players, String... args) {
        for (Player player : players) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Location base = player.getLocation();
                    for (int x = -RADIUS; x <= RADIUS; ++x) {
                        for (int y = -1; y <= 2; ++y) {
                            for (int z = -RADIUS; z <= RADIUS; ++z) {
                                Location location = base.clone().add(x, y, z);
                                Block block = location.getBlock();
                                if (REPLACE.contains(block.getType())) {
                                    location.getBlock().setType(STONE_MATERIAL);
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
