package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.common.util.Weighted;
import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge7.utils.BlockFinder;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.VEIN_COUNT;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.VEIN_RADIUS;

@Getter
public class VeinCommand extends ImmediateCommand {
	private final String effectName = "vein";

	public VeinCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Contract(value = "null -> fail", mutates = "param1")
	private static void randomlyShrinkOreVein(List<Location<World>> blockLocations) {
		if (blockLocations.isEmpty()) return;
		Collections.shuffle(blockLocations, random);
		int maxBlocks = 1 + random.nextInt(blockLocations.size());
		while (blockLocations.size() > maxBlocks)
			blockLocations.remove(0);
	}

	private static boolean isAir(Location<World> location) {
		return location.getBlock().getType().equals(BlockTypes.AIR);
	}

	private static boolean isNotAir(Location<World> location) {
		return !isAir(location);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse().type(Response.ResultType.RETRY).message("Could not find any blocks to replace");
		for (Player player : players) {
			BlockFinder finder = BlockFinder.builder()
					.origin(player.getLocation())
					.maxRadius(VEIN_RADIUS)
					.locationValidator(VeinCommand::isNotAir)
					.build();

			for (int iter = 0; iter < VEIN_COUNT; iter++) {
				Ores ore = RandomUtil.weightedRandom(Ores.values(), Ores.TOTAL_WEIGHTS);

				List<Location<World>> setBlocks = new ArrayList<>(8);
				Location<World> oreLocation = finder.next();
				if (oreLocation == null)
					continue;

				// get 2x2 chunk of blocks
				addOreVein(setBlocks, oreLocation);

				// if we didn't find viable blocks, exit
				if (setBlocks.isEmpty())
					continue;

				result.type(Response.ResultType.SUCCESS).message("SUCCESS");
				randomlyShrinkOreVein(setBlocks);

				if (!setBlocks.isEmpty())
					sync(() -> setBlocks.forEach(blockPos -> blockPos.setBlockType(ore.getBlock(), BlockChangeFlags.ALL)));
			}
		}
		return result;
	}

	// Gets a 2x2 chunk of blocks
	@Contract(value = "null, _ -> fail; _, null -> fail", mutates = "param1")
	private void addOreVein(List<Location<World>> stoneBlocks, Location<World> base) {
		for (int x = 0; x <= 1; ++x) {
			for (int y = 0; y <= 1; ++y) {
				for (int z = 0; z <= 1; ++z) {
					Location<World> loc = base.add(x, y, z);
					if (isNotAir(loc)) {
						stoneBlocks.add(loc);
					}
				}
			}
		}
	}

	@Getter
	public enum Ores implements Weighted {
		DIAMOND(BlockTypes.DIAMOND_ORE, 3),
		IRON(BlockTypes.IRON_ORE, 3),
		COAL(BlockTypes.COAL_ORE, 3),
		EMERALD(BlockTypes.EMERALD_ORE, 3),
		GOLD(BlockTypes.GOLD_ORE, 3),
		REDSTONE(BlockTypes.REDSTONE_ORE, 3),
		LAPIS(BlockTypes.LAPIS_ORE, 3),
		LAVA(BlockTypes.LAVA, 2)
		;

		public static final int TOTAL_WEIGHTS = Arrays.stream(values()).mapToInt(Ores::getWeight).sum();
		private final BlockType block;
		private final int weight;

		Ores(BlockType block, int weight) {
			this.block = block;
			this.weight = weight;
		}
	}
}
