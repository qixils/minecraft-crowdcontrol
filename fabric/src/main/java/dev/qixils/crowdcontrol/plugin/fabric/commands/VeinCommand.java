package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.util.CommonTags;
import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.common.util.Weighted;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.fabric.utils.BlockFinder;
import dev.qixils.crowdcontrol.plugin.fabric.utils.Location;
import dev.qixils.crowdcontrol.plugin.fabric.utils.TypedTag;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.VEIN_COUNT;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.VEIN_RADIUS;

@Getter
public class VeinCommand extends ImmediateCommand {
	private final TypedTag<Block> stones;
	private final String effectName = "vein";
	private final String displayName = "Spawn Ore Vein";

	public VeinCommand(FabricCrowdControlPlugin plugin) {
		super(plugin);
		stones = new TypedTag<>(CommonTags.STONES, Registry.BLOCK);
	}

	// Gets a 2x2 chunk of blocks
	@Contract(mutates = "param1, param2")
	private void addOreVein(List<Location> deepslateBlocks, List<Location> stoneBlocks, Location base) {
		for (int x = 0; x <= 1; ++x) {
			for (int y = 0; y <= 1; ++y) {
				for (int z = 0; z <= 1; ++z) {
					Location loc = base.add(x, y, z);
					Block matType = loc.block().getBlock();
					if (matType == Blocks.DEEPSLATE) {
						deepslateBlocks.add(loc);
					} else if (stones.contains(matType)) {
						stoneBlocks.add(loc);
					}
				}
			}
		}
	}

	@Contract(mutates = "param1")
	private static void randomlyShrinkOreVein(List<Location> blockLocations) {
		if (blockLocations.isEmpty()) return;
		Collections.shuffle(blockLocations, random);
		int maxBlocks = 1 + random.nextInt(blockLocations.size());
		while (blockLocations.size() > maxBlocks)
			blockLocations.remove(0);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse().type(Response.ResultType.FAILURE).message("Could not find any blocks to replace");
		for (ServerPlayer player : players) {
			BlockFinder finder = BlockFinder.builder()
					.origin(new Location(player))
					.maxRadius(VEIN_RADIUS)
					.locationValidator(location -> stones.contains(location.block().getBlock()))
					.build();

			for (int iter = 0; iter < VEIN_COUNT; iter++) {
				Ores ore = RandomUtil.weightedRandom(Ores.values(), Ores.TOTAL_WEIGHTS);

				List<Location> setBlocks = new ArrayList<>(8);
				List<Location> setDeepslateBlocks = new ArrayList<>(8);
				Location oreLocation = finder.next();
				if (oreLocation == null)
					continue;

				// get 2x2 chunk of blocks
				addOreVein(setDeepslateBlocks, setBlocks, oreLocation);

				// if we didn't find viable blocks, exit
				if (setBlocks.isEmpty() && setDeepslateBlocks.isEmpty())
					continue;

				result.type(Response.ResultType.SUCCESS).message("SUCCESS");
				randomlyShrinkOreVein(setBlocks);
				randomlyShrinkOreVein(setDeepslateBlocks);

				sync(() -> {
					setBlocks.forEach(blockPos -> blockPos.block(ore.getBlock().defaultBlockState()));
					setDeepslateBlocks.forEach(blockPos -> blockPos.block(ore.getDeepslateBlock().defaultBlockState()));
				});
			}
		}
		return result;
	}

	@Getter
	public enum Ores implements Weighted {
		DIAMOND(Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE, 3),
		IRON(Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE, 3),
		COAL(Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE, 3),
		EMERALD(Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE, 3),
		GOLD(Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE, 3),
		REDSTONE(Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE, 3),
		LAPIS(Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE, 3),
		ANCIENT_DEBRIS(Blocks.ANCIENT_DEBRIS, 1),
		QUARTZ(Blocks.NETHER_QUARTZ_ORE, 3),
		NETHER_GOLD(Blocks.NETHER_GOLD_ORE, 3),
		SILVERFISH(Blocks.INFESTED_STONE, Blocks.INFESTED_DEEPSLATE, 2),
		LAVA(Blocks.LAVA, 8);

		public static final int TOTAL_WEIGHTS = Arrays.stream(values()).mapToInt(Ores::getWeight).sum();
		private final Block block;
		private final Block deepslateBlock;
		private final int weight;

		Ores(Block block, Block deepslateBlock, int weight) {
			this.block = block;
			this.deepslateBlock = deepslateBlock;
			this.weight = weight;
		}

		Ores(Block block, int weight) {
			this(block, block, weight);
		}
	}
}
