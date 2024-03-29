package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.Command;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
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

abstract class NearbyLocationCommand<S> extends Command {
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
	public @NotNull CompletableFuture<@Nullable Builder> execute(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		CompletableFuture<Builder> future = new CompletableFuture<>();
		sync(() -> {
			Builder response = request.buildResponse().type(ResultType.FAILURE).message("Could not find a location to teleport to");
			for (Player player : players) {
				World world = player.getWorld();
				Location location = player.getLocation();
				S currentType = currentType(location);
				List<S> searchTypes = new ArrayList<>(getSearchTypes(world.getEnvironment()));
				Collections.shuffle(searchTypes, random);
				for (S searchType : searchTypes) {
					if (searchType.equals(currentType))
						continue;
					Location destination = safeLocation(search(location, searchType));
					if (destination == null)
						continue;
					if (destination.distanceSquared(location) <= 2500) // 50 blocks
						continue;
					if (!world.getWorldBorder().isInside(destination))
						continue;
					player.teleportAsync(destination).thenAccept(success -> {
						if (!success)
							return;
						player.sendActionBar(Component.translatable(
								"cc.effect.nearby_location.output",
								nameOf(searchType).color(NamedTextColor.YELLOW)
						));
					});
					response.type(ResultType.SUCCESS).message("SUCCESS"); // technically this could still fail; unlikely tho.
					break;
				}
			}
			future.complete(response);
		});
		return future;
	}
}
