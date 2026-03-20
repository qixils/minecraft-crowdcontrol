package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.util.CompletableFutureUtils;
import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.BiomeDefaultFeatures;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.HugeFungusConfiguration;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.levelgen.placement.FrequencyDecoratorConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

@Getter
public class PlantTreeCommand extends ModdedCommand {
	private final String effectName = "plant_tree";
	private static final List<ConfiguredFeature<?, ?>> TREES = List.of(
		Feature.TREE.configured(BiomeDefaultFeatures.NORMAL_TREE_CONFIG),
		Feature.TREE.configured(BiomeDefaultFeatures.NORMAL_TREE_WITH_BEES_0002_CONFIG),
		Feature.TREE.configured(BiomeDefaultFeatures.NORMAL_TREE_WITH_BEES_002_CONFIG),
		Feature.TREE.configured(BiomeDefaultFeatures.NORMAL_TREE_WITH_BEES_005_CONFIG),
		Feature.TREE.configured(BiomeDefaultFeatures.JUNGLE_TREE_CONFIG),
		Feature.TREE.configured(BiomeDefaultFeatures.JUNGLE_TREE_NOVINE_CONFIG),
		Feature.TREE.configured(BiomeDefaultFeatures.PINE_TREE_CONFIG),
		Feature.TREE.configured(BiomeDefaultFeatures.SPRUCE_TREE_CONFIG),
		Feature.TREE.configured(BiomeDefaultFeatures.ACACIA_TREE_CONFIG),
		Feature.TREE.configured(BiomeDefaultFeatures.BIRCH_TREE_CONFIG),
		Feature.TREE.configured(BiomeDefaultFeatures.BIRCH_TREE_WITH_BEES_0002_CONFIG),
		Feature.TREE.configured(BiomeDefaultFeatures.BIRCH_TREE_WITH_BEES_002_CONFIG),
		Feature.TREE.configured(BiomeDefaultFeatures.BIRCH_TREE_WITH_BEES_005_CONFIG),
		Feature.TREE.configured(BiomeDefaultFeatures.SUPER_BIRCH_TREE_WITH_BEES_0002_CONFIG),
		Feature.TREE.configured(BiomeDefaultFeatures.SWAMP_TREE_CONFIG),
		Feature.TREE.configured(BiomeDefaultFeatures.FANCY_TREE_CONFIG),
		Feature.TREE.configured(BiomeDefaultFeatures.FANCY_TREE_WITH_BEES_0002_CONFIG),
		Feature.TREE.configured(BiomeDefaultFeatures.FANCY_TREE_WITH_BEES_002_CONFIG),
		Feature.TREE.configured(BiomeDefaultFeatures.FANCY_TREE_WITH_BEES_005_CONFIG),
		Feature.TREE.configured(BiomeDefaultFeatures.JUNGLE_BUSH_CONFIG),
		Feature.TREE.configured(BiomeDefaultFeatures.DARK_OAK_TREE_CONFIG),
		Feature.TREE.configured(BiomeDefaultFeatures.MEGA_SPRUCE_TREE_CONFIG),
		Feature.TREE.configured(BiomeDefaultFeatures.MEGA_PINE_TREE_CONFIG),
		Feature.TREE.configured(BiomeDefaultFeatures.MEGA_JUNGLE_TREE_CONFIG),
		Feature.HUGE_FUNGUS.configured(HugeFungusConfiguration.HUGE_CRIMSON_FUNGI_NOT_PLANTED_CONFIG).decorated(FeatureDecorator.COUNT_HEIGHTMAP.configured(new FrequencyDecoratorConfiguration(8))),
		Feature.HUGE_FUNGUS.configured(HugeFungusConfiguration.HUGE_WARPED_FUNGI_NOT_PLANTED_CONFIG).decorated(FeatureDecorator.COUNT_HEIGHTMAP.configured(new FrequencyDecoratorConfiguration(8))),
		Feature.HUGE_RED_MUSHROOM.configured(BiomeDefaultFeatures.HUGE_RED_MUSHROOM_CONFIG),
		Feature.HUGE_BROWN_MUSHROOM.configured(BiomeDefaultFeatures.HUGE_BROWN_MUSHROOM_CONFIG)
	);

	public PlantTreeCommand(ModdedCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public void execute(@NotNull Supplier<List<ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			AtomicBoolean success = new AtomicBoolean();
			List<ServerPlayer> players = playerSupplier.get();
			Collection<CompletableFuture<?>> futures = new ArrayList<>(players.size());
			for (ServerPlayer player : players) {
				ConfiguredFeature<?, ?> treeType = RandomUtil.randomElementFrom(TREES); // TODO: test this one a lot
				CompletableFuture<Void> future = new CompletableFuture<>();
				futures.add(future);

				// the #canPlaceAt method sometimes erroneously trips up the async catcher
				// so this is run as sync to avoid confusing, useless errors
				sync(() -> {
					ServerLevel level = player.getLevel();
					if (treeType.place(level, level.structureFeatureManager(), level.getChunkSource().getGenerator(), level.random, player.blockPosition()))
						success.set(true);
					future.complete(null);
				});
			}

			// waits for all trees to get planted, then returns the resulting builder
			return CompletableFutureUtils.allOf(futures).thenApply($ -> success.get()
				? new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS)
				: new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Streamer is not in a suitable place for tree planting")).join();
		}));
	}
}
