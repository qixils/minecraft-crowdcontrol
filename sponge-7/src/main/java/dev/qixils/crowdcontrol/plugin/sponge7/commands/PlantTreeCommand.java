package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.common.util.CompletableFutureUtils;
import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.plugin.sponge7.Command;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.Builder;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gen.PopulatorObject;
import org.spongepowered.api.world.gen.PopulatorObjects;
import org.spongepowered.api.world.gen.type.BiomeTreeType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Getter
public class PlantTreeCommand extends Command {
	private final List<PopulatorObject> trees;
	private final String effectName = "plant_tree";

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
	public @NotNull CompletableFuture<Builder> execute(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder resp = request.buildResponse()
				.type(ResultType.RETRY)
				.message("Streamer is not in a suitable place for tree planting");
		PopulatorObject treeType = RandomUtil.randomElementFrom(trees);

		Collection<CompletableFuture<?>> futures = new ArrayList<>(players.size());
		for (Player player : players) {
			Location<World> location = player.getLocation();
			CompletableFuture<Void> future = new CompletableFuture<>();
			futures.add(future);

			// the #canPlaceAt method sometimes erroneously trips up the async catcher
			// so this is run as sync to avoid confusing, useless errors
			sync(() -> {
				if (treeType.canPlaceAt(location.getExtent(), location.getBlockX(), location.getBlockY(), location.getBlockZ())) {
					resp.type(ResultType.SUCCESS).message("SUCCESS");
					treeType.placeObject(location.getExtent(), random, location.getBlockX(), location.getBlockY(), location.getBlockZ());
				}
				future.complete(null);
			});
		}

		// waits for all trees to get planted, then returns the resulting builder
		return CompletableFutureUtils.allOf(futures).thenApply($ -> resp);
	}
}
