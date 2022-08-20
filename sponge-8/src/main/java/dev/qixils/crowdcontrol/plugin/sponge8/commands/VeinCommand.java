package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.common.util.CommonTags;
import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.common.util.Weighted;
import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge8.utils.BlockFinder;
import dev.qixils.crowdcontrol.plugin.sponge8.utils.TypedTag;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.VEIN_COUNT;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.VEIN_RADIUS;

@Getter
public class VeinCommand extends ImmediateCommand {
	private final TypedTag<BlockType> stones;
	private final String effectName = "vein";
	private final String displayName = "Spawn Ore Vein";

	public VeinCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
		stones = new TypedTag<>(CommonTags.STONES, plugin, RegistryTypes.BLOCK_TYPE);
	}

	@Contract(value = "null -> fail", mutates = "param1")
	private static void randomlyShrinkOreVein(List<ServerLocation> blockLocations) {
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
					.origin(player.serverLocation())
					.maxRadius(VEIN_RADIUS)
					.locationValidator(location -> stones.contains(location.blockType()))
					.build();

			for (int iter = 0; iter < VEIN_COUNT; iter++) {
				Ores ore = RandomUtil.weightedRandom(Ores.values(), Ores.TOTAL_WEIGHTS);

				List<ServerLocation> setBlocks = new ArrayList<>(8);
				// API9: deepslate
				ServerLocation oreLocation = finder.next();
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
	private void addOreVein(List<ServerLocation> stoneBlocks, ServerLocation base) {
		for (int x = 0; x <= 1; ++x) {
			for (int y = 0; y <= 1; ++y) {
				for (int z = 0; z <= 1; ++z) {
					ServerLocation loc = base.add(x, y, z);
					BlockType blockType = loc.blockType();
					if (stones.contains(blockType)) {
						stoneBlocks.add(loc);
					}
				}
			}
		}
	}

	@Getter // API9: add back deepslate
	public enum Ores implements Weighted {
		DIAMOND(BlockTypes.DIAMOND_ORE, 3),
		IRON(BlockTypes.IRON_ORE, 3),
		COAL(BlockTypes.COAL_ORE, 3),
		EMERALD(BlockTypes.EMERALD_ORE, 3),
		GOLD(BlockTypes.GOLD_ORE, 3),
		REDSTONE(BlockTypes.REDSTONE_ORE, 3),
		LAPIS(BlockTypes.LAPIS_ORE, 3),
		ANCIENT_DEBRIS(BlockTypes.ANCIENT_DEBRIS, 1),
		QUARTZ(BlockTypes.NETHER_QUARTZ_ORE, 3),
		NETHER_GOLD(BlockTypes.NETHER_GOLD_ORE, 3),
		SILVERFISH(BlockTypes.INFESTED_STONE, 2),
		LAVA(BlockTypes.LAVA, 8);

		public static final int TOTAL_WEIGHTS = Arrays.stream(values()).mapToInt(Ores::getWeight).sum();
		private final BlockType block;
		private final int weight;

		Ores(BlockType block, int weight) {
			this.block = block;
			this.weight = weight;
		}

		Ores(Supplier<BlockType> ore, int weight) {
			this(ore.get(), weight);
		}
	}
}
