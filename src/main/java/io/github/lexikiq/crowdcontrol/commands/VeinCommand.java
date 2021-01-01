package io.github.lexikiq.crowdcontrol.commands;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import io.github.lexikiq.crowdcontrol.ChatCommand;
import io.github.lexikiq.crowdcontrol.CrowdControl;
import io.github.lexikiq.crowdcontrol.utils.RandomUtil;
import io.github.lexikiq.crowdcontrol.utils.WeightedEnum;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.github.lexikiq.crowdcontrol.utils.BlockUtil.STONES;
import static io.github.lexikiq.crowdcontrol.utils.BlockUtil.STONES_SET;

public class VeinCommand extends ChatCommand {
    public VeinCommand(CrowdControl plugin) {
        super(plugin);
    }

    public enum Ores implements WeightedEnum {
        DIAMOND(Material.DIAMOND_ORE, 3),
        IRON(Material.IRON_ORE, 3),
        COAL(Material.COAL_ORE, 3),
        EMERALD(Material.EMERALD_ORE, 3),
        GOLD(Material.GOLD_ORE, 3),
        REDSTONE(Material.REDSTONE_ORE, 3),
        LAPIS(Material.LAPIS_ORE, 3),
        QUARTZ(Material.NETHER_QUARTZ_ORE, 1),
        NETHER_GOLD(Material.NETHER_GOLD_ORE, 1),
        SILVERFISH(Material.INFESTED_STONE, 16),
        LAVA(Material.LAVA, 7)
        ;

        private final @Getter Material block;
        private final @Getter int weight;
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
        return 60*3;
    }

    @Override
    public boolean execute(ChannelMessageEvent event, List<Player> players, String... args) {
        Material ore = ((Ores) RandomUtil.weightedRandom(Ores.values(), Ores.TOTAL_WEIGHTS)).getBlock();

        boolean didSomething = false;
        for (Player player : players) {
            List<Location> setBlocks = new ArrayList<>();
            Location oreLocation = RandomUtil.randomNearbyBlock(player.getLocation(), MIN_RADIUS, MAX_RADIUS, false, STONES);
            if (oreLocation == null) {
                continue;
            }
            // get 2x2 chunk of blocks
            for (int x = 0; x <= 1; ++x) {
                for (int y = 0; y <= 1; ++y) {
                    for (int z = 0; z <= 1; ++z) {
                        Location loc = oreLocation.clone().add(x, y, z);
                        if (STONES_SET.contains(loc.getBlock().getType())) {
                            setBlocks.add(loc);
                        }
                    }
                }
            }
            // if we found viable blocks (idk how we wouldn't have atleast one but justincase??)
            if (!setBlocks.isEmpty()) {
                didSomething = true;
                List<Location> trueSetBlocks = new ArrayList<>();
                int maxBlocks = 1+rand.nextInt(setBlocks.size());
                int blocksSet = 0;
                for (int i = 0; i < setBlocks.size() && blocksSet < maxBlocks; ++i) {
                    int blocksRemaining = maxBlocks-blocksSet;
                    int iterationsRemaining = setBlocks.size()-i;
                    if (blocksRemaining == iterationsRemaining || rand.nextBoolean()) {
                        ++blocksSet;
                        trueSetBlocks.add(setBlocks.get(i));
                    }
                }

                // probably needs to be run synchronously
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        for (Location blockPos : trueSetBlocks) {
                            blockPos.getBlock().setType(ore);
                        }
                    }
                }.runTask(plugin);
            }
        }
        return didSomething;
    }
}
