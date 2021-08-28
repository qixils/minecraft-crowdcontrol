package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.Command;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.utils.BlockUtil;
import dev.qixils.crowdcontrol.plugin.utils.RandomUtil;
import dev.qixils.crowdcontrol.plugin.utils.Weighted;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class VeinCommand extends Command {
    private final String effectName = "vein";
    private final String displayName = "Spawn Ore Vein";
    public VeinCommand(CrowdControlPlugin plugin) {
        super(plugin);
    }

    public enum Ores implements Weighted {
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
    public Response.@NotNull Result execute(@NotNull Request request) {
        Material ore = (RandomUtil.weightedRandom(Ores.values(), Ores.TOTAL_WEIGHTS)).getBlock();

        Response.Result result = new Response.Result(Response.ResultType.FAILURE, "Could not find any blocks to replace");
        for (Player player : CrowdControlPlugin.getPlayers()) {
            List<Location> setBlocks = new ArrayList<>();
            Location oreLocation = BlockUtil.BlockFinder.builder()
                    .origin(player.getLocation())
                    .maxRadius(MAX_RADIUS)
                    .minRadius(MIN_RADIUS)
                    .locationValidator(BlockUtil.STONES_TAG::matches)
                    .build().next();
            if (oreLocation == null) {
                continue;
            }
            // get 2x2 chunk of blocks
            for (int x = 0; x <= 1; ++x) {
                for (int y = 0; y <= 1; ++y) {
                    for (int z = 0; z <= 1; ++z) {
                        Location loc = oreLocation.clone().add(x, y, z);
                        if (BlockUtil.STONES_TAG.matches(loc)) {
                            setBlocks.add(loc);
                        }
                    }
                }
            }
            // if we found viable blocks (idk how we wouldn't have at least one but just in case??)
            if (!setBlocks.isEmpty()) {
                result = Response.Result.SUCCESS;
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

                Bukkit.getScheduler().runTask(plugin, () -> trueSetBlocks.forEach(blockPos -> blockPos.getBlock().setType(ore)));
            }
        }
        return result;
    }
}
