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
import net.minecraft.world.level.levelgen.feature.AbstractHugeMushroomFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.HugeFungusFeature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
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

	private static List<Feature> getTreesFor(Level level) {
		return level.registryAccess().lookupOrThrow(Registries.FEATURE)
				.stream()
				.filter(feature -> feature instanceof TreeFeature || feature instanceof HugeFungusFeature || feature instanceof AbstractHugeMushroomFeature)
				.collect(Collectors.toList());
	}

	@Override
	public void execute(@NotNull Supplier<List<ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			AtomicBoolean success = new AtomicBoolean();
			List<ServerPlayer> players = playerSupplier.get();
			Collection<CompletableFuture<?>> futures = new ArrayList<>(players.size());
			for (ServerPlayer player : players) {
				Feature treeType = RandomUtil.randomElementFrom(getTreesFor(player.level()));
				CompletableFuture<Void> future = new CompletableFuture<>();
				futures.add(future);

				// the #canPlaceAt method sometimes erroneously trips up the async catcher
				// so this is run as sync to avoid confusing, useless errors
				sync(() -> {
					ServerLevel level = player.level();
					if (treeType.place(level, level.getChunkSource().getGenerator(), level.getRandom(), player.blockPosition()))
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
