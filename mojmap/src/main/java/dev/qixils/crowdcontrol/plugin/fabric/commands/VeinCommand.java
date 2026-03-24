package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.common.util.Weighted;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.utils.BlockFinder;
import dev.qixils.crowdcontrol.plugin.fabric.utils.Location;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.VEIN_COUNT;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.VEIN_RADIUS;

@Getter
public class VeinCommand extends ModdedCommand {
	private final String effectName = "vein";

	public VeinCommand(ModdedCrowdControlPlugin plugin) {
		super(plugin);
	}

	// Gets a 2x2 chunk of blocks
	@Contract(mutates = "param1")
	private void addOreVein(List<Location> stoneBlocks, Location base) {
		for (int x = 0; x <= 2; ++x) {
			for (int y = 0; y <= 1; ++y) {
				for (int z = 0; z <= 2; ++z) {
					Location loc = base.add(x, y, z);
					BlockState block = loc.block();
					if (!block.isAir()) {
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
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			boolean success = false;
			for (ServerPlayer player : playerSupplier.get()) {
				BlockFinder finder = BlockFinder.builder()
					.origin(new Location(player))
					.maxRadius(VEIN_RADIUS)
					.locationValidator(loc -> !loc.block().isAir())
					.build();

				List<Ore> ores = new ArrayList<>();

				ores.add(new Ore(Blocks.COAL_ORE, 6));
				ores.add(new Ore(Blocks.DIAMOND_ORE, 6));
				ores.add(new Ore(Blocks.EMERALD_ORE, 6));
				ores.add(new Ore(Blocks.GOLD_ORE, 6));
				ores.add(new Ore(Blocks.IRON_ORE, 6));
				ores.add(new Ore(Blocks.LAPIS_ORE, 6));
				ores.add(new Ore(Blocks.REDSTONE_ORE, 6));
				ores.add(new Ore(Blocks.NETHER_GOLD_ORE, 6));
				ores.add(new Ore(Blocks.NETHER_QUARTZ_ORE, 6));
				ores.add(new Ore(Blocks.ANCIENT_DEBRIS, 3));
				// troll blocks
				ores.add(new Ore(Blocks.LAVA, 1));
				ores.add(new Ore(Blocks.INFESTED_STONE, 1));

				for (int iter = 0; iter < VEIN_COUNT; iter++) {
					Ore ore = RandomUtil.weightedRandom(ores);

					List<Location> setBlocks = new ArrayList<>(8);
					Location oreLocation = finder.next();
					if (oreLocation == null)
						continue;

					// get 2x2 chunk of blocks
					addOreVein(setBlocks, oreLocation);

					// if we didn't find viable blocks, exit
					if (setBlocks.isEmpty())
						continue;

					success = true;
					randomlyShrinkOreVein(setBlocks);

					sync(() -> setBlocks.forEach(blockPos -> blockPos.block(ore.getBlock().defaultBlockState())));
				}
			}
			return success
				? new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS)
				: new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Could not find any blocks to replace");
		}));
	}

	@Getter
	public static class Ore implements Weighted {
		private final Block block;
		private final int weight;

		Ore(Block block, int weight) {
			this.block = block;
			this.weight = weight;
		}
	}
}
