package io.github.lexikiq.crowdcontrol.commands;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.google.common.collect.ImmutableList;
import io.github.lexikiq.crowdcontrol.ChatCommand;
import io.github.lexikiq.crowdcontrol.CrowdControl;
import io.github.lexikiq.crowdcontrol.utils.RandomUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class FlowerCommand extends ChatCommand {
    protected static final int RADIUS = 10;
    protected static final int MIN_RAND = 14;  // inclusive
    protected static final int MAX_RAND = 28;  // inclusive
    protected static final List<Material> FLOWERS = ImmutableList.of(
            Material.POPPY,
            Material.DANDELION,
            Material.BLUE_ORCHID,
            Material.ALLIUM,
            Material.AZURE_BLUET,
            Material.ORANGE_TULIP,
            Material.RED_TULIP,
            Material.PINK_TULIP,
            Material.WHITE_TULIP,
            Material.OXEYE_DAISY,
            Material.CORNFLOWER,
            Material.LILY_OF_THE_VALLEY,
            Material.WITHER_ROSE
    );

    public FlowerCommand(CrowdControl plugin) {
        super(plugin);
    }

    @Override
    public int getCooldownSeconds() {
        return 0;
    }

    @Override
    public @NotNull String getCommand() {
        return "flowers";
    }

    @Override
    public boolean execute(ChannelMessageEvent event, List<Player> players, String... args) {
        for (Player player : players) {
            List<Location> locations = RandomUtil.randomNearbyBlocks(player.getLocation(), RADIUS, false, CrowdControl.AIR_ARRAY);
            if (locations.isEmpty()) {continue;} // avoid scheduling a task if unnecessary
            Collections.shuffle(locations, rand);
            new BukkitRunnable() {
                @Override
                public void run() {
                    int placed = 0;
                    int toPlace = MIN_RAND+rand.nextInt(MAX_RAND-MIN_RAND+1);
                    for (Location location : locations) {
                        if (location.clone().subtract(0, 1, 0).getBlock().getType() == Material.GRASS_BLOCK) {
                            ++placed;
                            location.getBlock().setType((Material) RandomUtil.randomElementFrom(FLOWERS));
                            if (placed == toPlace) {
                                break;
                            }
                        }
                    }
                }
            }.runTask(plugin);
        }
        return true;
    }
}
