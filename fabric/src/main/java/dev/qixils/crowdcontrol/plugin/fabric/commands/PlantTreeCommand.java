package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.util.CompletableFutureUtils;
import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.plugin.fabric.Command;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.Builder;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Getter
public class PlantTreeCommand extends Command {
	private final String effectName = "plant_tree";

	public PlantTreeCommand(FabricCrowdControlPlugin plugin) {
		super(plugin);
	}

	private static List<ConfiguredFeature<?, ?>> getTreesFor(World level) {
		return level.getRegistryManager().getOptional(RegistryKeys.CONFIGURED_FEATURE)
				.orElseThrow(() -> new IllegalStateException("No configured feature registry"))
				.stream()
				.filter(feature -> {
					FeatureConfig c = feature.config();
					return c instanceof TreeFeatureConfig || c instanceof HugeFungusFeatureConfig || c instanceof HugeMushroomFeatureConfig;
				})
				.collect(Collectors.toList());
	}

	@Override
	public @NotNull CompletableFuture<Builder> execute(@NotNull List<@NotNull ServerPlayerEntity> players, @NotNull Request request) {
		Response.Builder resp = request.buildResponse()
				.type(ResultType.RETRY)
				.message("Streamer is not in a suitable place for tree planting");

		Collection<CompletableFuture<?>> futures = new ArrayList<>(players.size());
		for (ServerPlayerEntity player : players) {
			ConfiguredFeature<?, ?> treeType = RandomUtil.randomElementFrom(getTreesFor(player.world));
			CompletableFuture<Void> future = new CompletableFuture<>();
			futures.add(future);

			// the #canPlaceAt method sometimes erroneously trips up the async catcher
			// so this is run as sync to avoid confusing, useless errors
			sync(() -> {
				ServerWorld level = player.getWorld();
				if (treeType.generate(level, level.getChunkManager().getChunkGenerator(), level.random, player.getBlockPos()))
					resp.type(ResultType.SUCCESS).message("SUCCESS");
				future.complete(null);
			});
		}

		// waits for all trees to get planted, then returns the resulting builder
		return CompletableFutureUtils.allOf(futures).thenApply($ -> resp);
	}
}
