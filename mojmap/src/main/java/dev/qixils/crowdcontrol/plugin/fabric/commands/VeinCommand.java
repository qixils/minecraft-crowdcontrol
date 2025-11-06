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
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.VEIN_COUNT;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.VEIN_RADIUS;

@Getter
public class VeinCommand extends ModdedCommand {
	// we don't have fabric api imported anymore so we have to define the common tags manually
	public static final TagKey<Block> ORES = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "ores"));
	public static final TagKey<Block> ORE_BEARING_GROUND_DEEPSLATE = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "ore_bearing_ground/deepslate"));

	private final String effectName = "vein";

	public VeinCommand(ModdedCrowdControlPlugin plugin) {
		super(plugin);
	}

	// Gets a 2x2 chunk of blocks
	@Contract(mutates = "param1, param2")
	private void addOreVein(List<Location> deepslateBlocks, List<Location> stoneBlocks, Location base) {
		for (int x = 0; x <= 2; ++x) {
			for (int y = 0; y <= 1; ++y) {
				for (int z = 0; z <= 2; ++z) {
					Location loc = base.add(x, y, z);
					BlockState block = loc.block();
					if (block.is(ORE_BEARING_GROUND_DEEPSLATE)) {
						deepslateBlocks.add(loc);
					} else if (!block.isAir()) {
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

				Registry<Block> registry = player.level().registryAccess().lookupOrThrow(Registries.BLOCK);
				List<Ore> ores = new ArrayList<>();

				// troll blocks
				ores.add(new Ore(registry.wrapAsHolder(Blocks.LAVA), 1));
				ores.add(new Ore(registry.wrapAsHolder(Blocks.INFESTED_STONE), registry.wrapAsHolder(Blocks.INFESTED_DEEPSLATE), 1));

				registry.getOrThrow(ORES).stream()
					.filter(Holder::isBound) // failsafe
					.sorted((a, b) -> {
						ResourceLocation keyA = a.unwrapKey().get().location();
						ResourceLocation keyB = b.unwrapKey().get().location();
						boolean deepslateA = keyA.value().startsWith("deepslate_");
						boolean deepslateB = keyB.value().startsWith("deepslate_");
						if (deepslateA != deepslateB) return deepslateA ? 1 : -1;
						return keyA.asString().compareTo(keyB.asString());
					})
					.forEachOrdered(item -> {
						ResourceLocation location = item.unwrapKey().get().location();
						if (location.value().startsWith("deepslate_")) {
							Optional<Ore> matching = ores.stream().filter(ore -> {
								ResourceLocation oreLoc = ore.getBlock().unwrapKey().get().location();
								if (!location.namespace().equals(oreLoc.namespace())) return false;
								if (!location.value().equals("deepslate_" + oreLoc.value())) return false;
								return true;
							}).findFirst();
							if (matching.isPresent()) {
								int idx = ores.indexOf(matching.get());
								if (idx != -1) { // failsafe
									ores.set(idx, matching.get().withDeepslate(item));
									return;
								}
							}
						}

						ores.add(new Ore(item, item.is(registry.wrapAsHolder(Blocks.ANCIENT_DEBRIS)) ? 3 : 6));
					});

				for (int iter = 0; iter < VEIN_COUNT; iter++) {
					Ore ore = RandomUtil.weightedRandom(ores);

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

					success = true;
					randomlyShrinkOreVein(setBlocks);
					randomlyShrinkOreVein(setDeepslateBlocks);

					sync(() -> {
						setBlocks.forEach(blockPos -> blockPos.block(ore.getBlock().value().defaultBlockState()));
						setDeepslateBlocks.forEach(blockPos -> blockPos.block(ore.getDeepslateBlock().value().defaultBlockState()));
					});
				}
			}
			return success
				? new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS)
				: new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Could not find any blocks to replace");
		}));
	}

	@Getter
	public static class Ore implements Weighted {
		private final Holder<Block> block;
		private final Holder<Block> deepslateBlock;
		private final int weight;

		Ore(Holder<Block> block, Holder<Block> deepslateBlock, int weight) {
			this.block = block;
			this.deepslateBlock = deepslateBlock;
			this.weight = weight;
		}

		Ore(Holder<Block> block, int weight) {
			this(block, block, weight);
		}

		public Ore withDeepslate(Holder<Block> deepslateBlock) {
			return new Ore(this.block, deepslateBlock, weight);
		}
	}
}
