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
import java.util.stream.Collectors;

@Getter
public class PlantTreeCommand extends Command {
	private final String effectName = "plant_tree";

	public PlantTreeCommand(FabricCrowdControlPlugin plugin) {
		super(plugin);
	}

	private static List<ConfiguredFeature<?, ?>> getTreesFor(Level level) {
		return level.registryAccess().registry(Registries.CONFIGURED_FEATURE)
				.orElseThrow(() -> new IllegalStateException("No configured feature registry"))
				.stream()
				.filter(feature -> {
					FeatureConfiguration c = feature.config();
					return c instanceof TreeConfiguration || c instanceof HugeFungusConfiguration || c instanceof HugeMushroomFeatureConfiguration;
				})
				.collect(Collectors.toList());
	}

	@Override
	public @NotNull CompletableFuture<Builder> execute(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder resp = request.buildResponse()
				.type(ResultType.RETRY)
				.message("Streamer is not in a suitable place for tree planting");

		Collection<CompletableFuture<?>> futures = new ArrayList<>(players.size());
		for (ServerPlayer player : players) {
			ConfiguredFeature<?, ?> treeType = RandomUtil.randomElementFrom(getTreesFor(player.level));
			CompletableFuture<Void> future = new CompletableFuture<>();
			futures.add(future);

			// the #canPlaceAt method sometimes erroneously trips up the async catcher
			// so this is run as sync to avoid confusing, useless errors
			sync(() -> {
				ServerLevel level = player.getLevel();
				if (treeType.place(level, level.getChunkSource().getGenerator(), level.random, player.blockPosition()))
					resp.type(ResultType.SUCCESS).message("SUCCESS");
				future.complete(null);
			});
		}

		// waits for all trees to get planted, then returns the resulting builder
		return CompletableFutureUtils.allOf(futures).thenApply($ -> resp);
	}
}
