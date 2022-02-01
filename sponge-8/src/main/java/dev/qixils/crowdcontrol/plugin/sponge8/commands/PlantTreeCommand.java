package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.common.util.CompletableFutureUtils;
import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.plugin.sponge8.Command;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.Builder;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.gen.PopulatorObject;
import org.spongepowered.api.world.gen.PopulatorObjects;
import org.spongepowered.api.world.gen.type.BiomeTreeType;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Getter
public class PlantTreeCommand extends Command {
	private final List<PopulatorObject> trees;
	private final String effectName = "plant_tree";
	private final String displayName = "Plant Tree";

	public PlantTreeCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
		// create trees list
		List<PopulatorObject> trees = new ArrayList<>(20);
		for (BiomeTreeType treeType : plugin.getRegistry().getAllOf(BiomeTreeType.class)) {
			trees.add(treeType.getPopulatorObject());
			if (treeType.getLargePopulatorObject().isPresent())
				trees.add(treeType.getLargePopulatorObject().get());
		}
		// mushrooms (to match Paper impl tbh)
		trees.add(PopulatorObjects.RED);
		trees.add(PopulatorObjects.BROWN);

		this.trees = Collections.unmodifiableList(trees);
	}

	@Override
	public @NotNull CompletableFuture<Builder> execute(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder resp = request.buildResponse()
				.type(ResultType.RETRY)
				.message("Streamer is not in a suitable place for tree planting");
		PopulatorObject treeType = RandomUtil.randomElementFrom(trees);

		Collection<CompletableFuture<?>> futures = new ArrayList<>(players.size());
		for (ServerPlayer player : players) {
			ServerLocation location = player.serverLocation();
			CompletableFuture<Void> future = new CompletableFuture<>();
			futures.add(future);

			// the #canPlaceAt method sometimes erroneously trips up the async catcher
			// so this is run as sync to avoid confusing, useless errors
			sync(() -> {
				if (treeType.canPlaceAt(location.world(), location.blockX(), location.blockY(), location.blockZ())) {
					resp.type(ResultType.SUCCESS).message("SUCCESS");
					treeType.placeObject(location.world(), random, location.blockX(), location.blockY(), location.blockZ());
				}
				future.complete(null);
			});
		}

		// waits for all trees to get planted, then returns the resulting builder
		return CompletableFutureUtils.allOf(futures).thenApply($ -> resp);
	}
}
