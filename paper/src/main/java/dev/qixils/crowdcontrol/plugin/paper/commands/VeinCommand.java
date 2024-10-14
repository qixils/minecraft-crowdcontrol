package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.common.util.Weighted;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.RegionalCommandSync;
import dev.qixils.crowdcontrol.plugin.paper.utils.BlockUtil.BlockFinder;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.VEIN_COUNT;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.VEIN_RADIUS;

@Getter
public class VeinCommand extends RegionalCommandSync {
	private final String effectName = "vein";

	public VeinCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	// Gets a 2x2 chunk of blocks
	@Contract(mutates = "param1, param2")
	private static void addOreVein(List<Location> deepslateBlocks, List<Location> stoneBlocks, Location base) {
		for (int x = 0; x <= 1; ++x) {
			for (int y = 0; y <= 1; ++y) {
				for (int z = 0; z <= 1; ++z) {
					Location loc = base.clone().add(x, y, z);
					Block block = loc.getBlock();
					Material matType = block.getType();
					if (matType == Material.DEEPSLATE) {
						deepslateBlocks.add(loc);
					} else if (!block.isEmpty()) {
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
			blockLocations.removeFirst();
	}

	@Override
	protected Response.@NotNull Builder buildFailure(@NotNull Request request) {
		return request.buildResponse()
			.type(Response.ResultType.RETRY)
			.message("Could not find any blocks to replace");
	}

	@Override
	protected boolean executeRegionallySync(@NotNull Player player, @NotNull Request request) {
		BlockFinder finder = BlockFinder.builder()
			.origin(player.getLocation())
			.maxRadius(VEIN_RADIUS)
			.locationValidator(loc -> !loc.getBlock().isEmpty())
			.build();

		boolean success = false;

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

			randomlyShrinkOreVein(setBlocks);
			randomlyShrinkOreVein(setDeepslateBlocks);

			setBlocks.forEach(blockPos -> blockPos.getBlock().setType(ore.getBlock()));
			setDeepslateBlocks.forEach(blockPos -> blockPos.getBlock().setType(ore.getDeepslateBlock()));

			success = true;
		}

		return success;
	}

	@Getter
	public enum Ores implements Weighted {
		DIAMOND(Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE, 3),
		IRON(Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE, 3),
		COAL(Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE, 3),
		EMERALD(Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE, 3),
		GOLD(Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE, 3),
		REDSTONE(Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE, 3),
		LAPIS(Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE, 3),
		ANCIENT_DEBRIS(Material.ANCIENT_DEBRIS, 1),
		QUARTZ(Material.NETHER_QUARTZ_ORE, 3),
		NETHER_GOLD(Material.NETHER_GOLD_ORE, 3),
		SILVERFISH(Material.INFESTED_STONE, Material.INFESTED_DEEPSLATE, 2),
		LAVA(Material.LAVA, 8);

		public static final int TOTAL_WEIGHTS = Arrays.stream(values()).mapToInt(Ores::getWeight).sum();
		private final Material block;
		private final Material deepslateBlock;
		private final int weight;

		Ores(Material block, Material deepslateBlock, int weight) {
			this.block = block;
			this.deepslateBlock = deepslateBlock;
			this.weight = weight;
		}

		Ores(Material block, int weight) {
			this(block, block, weight);
		}
	}
}
