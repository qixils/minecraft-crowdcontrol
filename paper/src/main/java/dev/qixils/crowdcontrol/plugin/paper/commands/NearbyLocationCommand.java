package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.RegionalCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response.Builder;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedStage;

abstract class NearbyLocationCommand<S> extends RegionalCommand {
	protected NearbyLocationCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Nullable
	private static Location highestLocation(final @Nullable Location origin) {
		if (origin == null)
			return null;

		int air = 0;
		World world = origin.getWorld();
		Location location = new Location(world, origin.getX(), world.getLogicalHeight() - 1, origin.getZ(), origin.getYaw(), origin.getPitch());
		while (true) {
			Block block = location.getBlock();
			if (location.getBlockY() < (world.getMinHeight() + 1)) // idk if the +1 is necessary but why not
				return null;
			else if (!block.isBuildable() && !block.isLiquid() && !block.isSolid()) // roughly equal to the fabric checks. perhaps a little redundant.
				air += 1;
			else if (air >= 1)
				break;
			else
				air = 0;
			location.subtract(0, 1, 0);
		}
		return location;
	}

	@Nullable
	private static Location safeLocation(final @Nullable Location origin) {
		// get the highest solid block with 2+ air blocks above it
		Location location = highestLocation(origin);
		if (location == null)
			return null;

		// ensure player does not get placed on a dangerous block while chunks load
		Block block = location.getBlock();
		Material type = block.getType();
		if (type == Material.FIRE)
			block.setType(Material.AIR);
		else if (!type.isSolid())
			block.setType(Material.GLASS);

		// place player on top of the block
		location.add(0.5, 1, 0.5);

		// successfully found a safe location !
		return location;
	}

	@Nullable
	protected abstract Location search(@NotNull Location origin, @NotNull S searchType);

	@NotNull
	protected abstract Collection<S> getSearchTypes(@NotNull Environment environment);

	protected abstract @NotNull Component nameOf(@NotNull S searchType);

	@Nullable
	protected S currentType(@NotNull Location origin) {
		return null;
	}

	@Override
	protected @NotNull Builder buildFailure(@NotNull Request request) {
		return request.buildResponse().type(ResultType.FAILURE).message("Could not find a location to teleport to");
	}

	@Override
	protected CompletableFuture<Boolean> executeRegionallyAsync(@NotNull Player player, @NotNull Request request) {
		World world = player.getWorld();
		Location location = player.getLocation();
		S currentType = currentType(location);
		List<S> searchTypes = new ArrayList<>(getSearchTypes(world.getEnvironment()));
		Collections.shuffle(searchTypes, random);

		CompletableFuture<Boolean> success = CompletableFuture.completedFuture(false);
		for (S searchType : searchTypes) {
			success = success.thenCompose(result -> {
				if (result) return completedStage(true);

				if (searchType.equals(currentType))
					return completedStage(false);
				Location destination = safeLocation(search(location, searchType));
				if (destination == null)
					return completedStage(false);
				if (destination.distanceSquared(location) <= 2500) // 50 blocks
					return completedStage(false);
				if (!world.getWorldBorder().isInside(destination))
					return completedStage(false);

				return player.teleportAsync(destination).thenApply(tpSuccess -> {
					if (!tpSuccess) return false;

					player.sendActionBar(Component.translatable(
						"cc.effect.nearby_location.output",
						nameOf(searchType).color(NamedTextColor.YELLOW)
					));
					return true;
				});
			});
		}

		return success;
	}
}
