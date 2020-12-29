package io.github.lexikiq.crowdcontrol.commands;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import io.github.lexikiq.crowdcontrol.ChatCommand;
import io.github.lexikiq.crowdcontrol.CrowdControl;
import io.github.lexikiq.crowdcontrol.utils.RandomUtil;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class VeinCommand extends ChatCommand {
    public VeinCommand(CrowdControl plugin) {
        super(plugin);
    }

    public static final Material[] STONES = new Material[]{
            Material.ANDESITE,
            Material.DIORITE,
            Material.STONE,
            Material.DIRT,
            Material.GRAVEL,
            Material.GRANITE,
            Material.NETHERRACK
    };

    public enum Ores {
        DIAMOND(Material.DIAMOND_ORE, 3),
        IRON(Material.IRON_ORE, 3),
        COAL(Material.COAL_ORE, 3),
        EMERALD(Material.EMERALD_ORE, 3),
        GOLD(Material.GOLD_ORE, 3),
        REDSTONE(Material.REDSTONE_ORE, 3),
        LAPIS(Material.LAPIS_ORE, 3),
        QUARTZ(Material.NETHER_QUARTZ_ORE, 1),
        NETHER_GOLD(Material.NETHER_GOLD_ORE, 1),
        SILVERFISH(Material.INFESTED_STONE, 23)
        ;

        public final @Getter Material block;
        public final @Getter int weight;
        Ores(Material block, int weight) {
            this.block = block;
            this.weight = weight;
        }

        public static final int TOTAL_WEIGHTS = Arrays.stream(values()).mapToInt(Ores::getWeight).sum();
    }

    public static final int MIN_RADIUS = 10;
    public static final int MAX_RADIUS = 20;

    @Override
    public @NotNull String getCommand() {
        return "vein";
    }

    @Override
    public int getCooldownSeconds() {
        return 60*5;
    }

    @Override
    public boolean execute(ChannelMessageEvent event, Collection<? extends Player> players) {
        // Weighted random code based off of https://stackoverflow.com/a/6737362
        Ores[] items = Ores.values();
        int idx = 0;
        for (double r = Math.random() * Ores.TOTAL_WEIGHTS; idx < items.length - 1; ++idx) {
            r -= items[idx].getWeight();
            if (r <= 0.0) break;
        }
        Material ore = items[idx].getBlock();

        plugin.getLogger().info("avery is hot");
        boolean didSomething = false;
        for (Player player : players) {
            List<Location> setBlocks = new ArrayList<>();
            Location oreLocation = RandomUtil.randomNearbyBlock(player.getLocation(), MIN_RADIUS, MAX_RADIUS, false, STONES);
            if (oreLocation == null) {
                continue;
            }
            plugin.getLogger().info("pogchamp");
            // get 2x2 chunk of blocks
            for (int x = 0; x <= 1; ++x) {
                for (int y = 0; y <= 1; ++y) {
                    for (int z = 0; z <= 1; ++z) {
                        Location loc = oreLocation.clone().add(x, y, z);
                        if (Arrays.stream(STONES).anyMatch((m) -> m == loc.getBlock().getType())) {
                            setBlocks.add(loc);
                        }
                    }
                }
            }
            plugin.getLogger().info(String.valueOf(setBlocks.size()));
            // if we found viable blocks (idk how we wouldn't have atleast one but justincase??)
            if (setBlocks.size() > 0) {
                didSomething = true;
                List<Location> trueSetBlocks = new ArrayList<>();
                int maxBlocks = 1+rand.nextInt(setBlocks.size());
                plugin.getServer().broadcastMessage(String.valueOf(maxBlocks));
                int blocksSet = 0;
                for (int i = 0; i < setBlocks.size() && blocksSet < maxBlocks; ++i) {
                    int blocksRemaining = maxBlocks-blocksSet;
                    int iterationsRemaining = setBlocks.size()-i;
                    if (blocksRemaining == iterationsRemaining || rand.nextBoolean()) {
                        ++blocksSet;
                        trueSetBlocks.add(setBlocks.get(i));
                    }
                }
                plugin.getLogger().info(String.valueOf(trueSetBlocks.size()));

                // probably needs to be run synchronously
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        for (Location blockPos : trueSetBlocks) {
                            blockPos.getBlock().setType(ore);
                        }
                    }
                }.runTask(plugin);
                plugin.getLogger().info("---");
            }
        }
        return didSomething;
    }
}
