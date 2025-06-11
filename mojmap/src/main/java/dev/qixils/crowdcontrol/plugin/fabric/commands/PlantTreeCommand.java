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
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.HugeFungusConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Getter
public class PlantTreeCommand extends ModdedCommand {
	private final String effectName = "plant_tree";

	public PlantTreeCommand(ModdedCrowdControlPlugin plugin) {
		super(plugin);
	}

	private static List<ConfiguredFeature<?, ?>> getTreesFor(Level level) {
		return level.registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE)
				.stream()
				.filter(feature -> {
					FeatureConfiguration c = feature.config();
					return c instanceof TreeConfiguration || c instanceof HugeFungusConfiguration || c instanceof HugeMushroomFeatureConfiguration;
				})
				.collect(Collectors.toList());
	}

	@Override
	public void execute(@NotNull Supplier<List<ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			AtomicBoolean success = new AtomicBoolean();
			List<ServerPlayer> players = playerSupplier.get();
			Collection<CompletableFuture<?>> futures = new ArrayList<>(players.size());
			for (ServerPlayer player : players) {
				ConfiguredFeature<?, ?> treeType = RandomUtil.randomElementFrom(getTreesFor(player.serverLevel()));
				CompletableFuture<Void> future = new CompletableFuture<>();
				futures.add(future);

				// the #canPlaceAt method sometimes erroneously trips up the async catcher
				// so this is run as sync to avoid confusing, useless errors
				sync(() -> {
					ServerLevel level = player.serverLevel();
					if (treeType.place(level, level.getChunkSource().getGenerator(), level.random, player.blockPosition()))
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
