package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.plugin.fabric.Command;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.utils.Location;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response.Builder;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

abstract class NearbyLocationCommand<S> extends Command {
	protected NearbyLocationCommand(FabricCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Nullable
	private static Location highestLocation(final @Nullable Location origin) {
		if (origin == null)
			return null;

		int air = 0;
		final ServerLevel world = origin.level();
		Location location = new Location(world, origin.x(), world.getLogicalHeight() - 1, origin.z(), origin.yaw(), origin.pitch());
		while (true) {
			BlockState block = location.block();
			if (location.y() < (world.getMinBuildHeight() + 1)) // idk if the +1 is necessary but why not
				return null;
			else if (block.getBlock().isPossibleToRespawnInThis(block) && !block.blocksMotion())
				air += 1;
			else if (air >= 1)
				break;
			else
				air = 0;
			location = location.add(0, -1, 0);
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
		BlockState block = location.block();
		Block type = block.getBlock();
		if (Blocks.FIRE.equals(type))
			location.block(Blocks.AIR.defaultBlockState());
		else if (!type.isPossibleToRespawnInThis(block))
			location.block(Blocks.GLASS.defaultBlockState());

		// place player on top of the block
		location = location.add(0.5, 1, 0.5);

		// successfully found a safe location !
		return location;
	}

	@Nullable
	protected abstract Location search(@NotNull Location origin, @NotNull S searchType);

	@NotNull
	protected abstract Collection<S> getSearchTypes(@NotNull ServerLevel level);

	protected abstract @NotNull Component nameOf(@NotNull S searchType);

	@Nullable
	protected S currentType(@NotNull Location origin) {
		return null;
	}

	@Override
	public @NotNull CompletableFuture<@Nullable Builder> execute(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		CompletableFuture<Builder> future = new CompletableFuture<>();
		sync(() -> {
			Builder response = request.buildResponse().type(ResultType.FAILURE).message("Could not find a location to teleport to");
			for (ServerPlayer player : players) {
				ServerLevel world = player.getLevel();
				Location location = new Location(player);
				S currentType = currentType(location);
				List<S> searchTypes = new ArrayList<>(getSearchTypes(world));
				Collections.shuffle(searchTypes, random);
				for (S searchType : searchTypes) {
					if (searchType.equals(currentType))
						continue;
					Location target = search(location, searchType);
					if (target == null)
						continue;
					Location destination = safeLocation(target);
					if (destination == null) {
						plugin.getSLF4JLogger().debug("Skipping {} because it is not safe", searchType);
						continue;
					}
					// TODO: feature parity issue -- this length check uses only X/Z while other platforms use X/Y/Z. former is probably preferable.
					if (Mth.lengthSquared(destination.x() - location.x(), destination.z() - location.z()) <= 2500) // 50 blocks
						continue;
					if (!world.getWorldBorder().isWithinBounds(destination.x(), destination.z()))
						continue;
					player.teleportTo(destination.x(), destination.y(), destination.z());
					player.sendActionBar(Component.translatable(
							"cc.effect.nearby_location.output",
							nameOf(searchType).color(NamedTextColor.YELLOW)
					));
					response.type(ResultType.SUCCESS).message("SUCCESS"); // technically this could still fail; unlikely tho.
					break;
				}
			}
			future.complete(response);
		});
		return future;
	}
}
