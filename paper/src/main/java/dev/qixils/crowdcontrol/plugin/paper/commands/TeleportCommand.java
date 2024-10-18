package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.RegionalCommand;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.EAT_CHORUS_FRUIT_MAX_RADIUS;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.EAT_CHORUS_FRUIT_MIN_RADIUS;

@Getter
public class TeleportCommand extends RegionalCommand {
	private final String effectName = "chorus_fruit";

	public TeleportCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	private static double nextDoubleOffset() {
		double value = RandomUtil.RNG.nextDouble(EAT_CHORUS_FRUIT_MIN_RADIUS, EAT_CHORUS_FRUIT_MAX_RADIUS);
		if (RandomUtil.RNG.nextBoolean()) {
			value = -value;
		}
		return value;
	}

	private static int nextIntOffset() {
		int value = RandomUtil.RNG.nextInt(EAT_CHORUS_FRUIT_MIN_RADIUS, EAT_CHORUS_FRUIT_MAX_RADIUS);
		if (RandomUtil.RNG.nextBoolean()) {
			value = -value;
		}
		return value;
	}

	@Override
	protected @Nullable CCEffectResponse precheck(@NotNull List<@NotNull Player> players, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		if (isActive(ccPlayer, "walk", "look"))
			return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Cannot fling while frozen");
		return null;
	}

	@Override
	protected @NotNull CCEffectResponse buildFailure(@NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "No teleportation destinations were available");
	}

	@Override
	protected CompletableFuture<Boolean> executeRegionallyAsync(@NotNull Player player, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		return CompletableFuture.supplyAsync(() -> {
			// TODO: passengers
			Location loc = player.getLocation();
			World level = loc.getWorld();
			double x = loc.getX();
			double y = loc.getY();
			double z = loc.getZ();
			for (int i = 0; i < 16; ++i) {
				double destX = x + nextDoubleOffset();
				double destY = Math.clamp(y + nextIntOffset(), level.getMinHeight(), level.getMinHeight() + level.getLogicalHeight() - 1);
				double destZ = z + nextDoubleOffset();
				if (!randomTeleport(player, destX, destY, destZ)) continue;
				// play sound
				level.playSound(loc, Sound.ITEM_CHORUS_FRUIT_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.0f);
				player.playSound(player, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1.0f, 1.0f);
				return true;
			}
			return false;
		}, getPlugin().getSyncExecutor());
	}

	public boolean randomTeleport(Player player, double destX, double destY, double destZ) {
		Location loc = player.getLocation();
		Location dest = loc.clone().set(destX, destY, destZ);
		int chunkX = (int) Math.floor(destX) >> 4;
		int chunkZ = (int) Math.floor(destZ) >> 4;
		World world = player.getWorld();
		if (!world.isChunkLoaded(chunkX, chunkZ))
			return false;
		while (dest.getY() > world.getMinHeight()) {
			Block block = world.getBlockAt(dest.clone().subtract(0, 1, 0));
			if (block.isCollidable()) {
				player.teleport(dest); // TODO: folia
				BoundingBox bb = player.getBoundingBox();
				if (!world.hasCollisionsIn(bb) && !containsAnyLiquid(world, bb)) {
					player.playEffect(EntityEffect.TELEPORT_ENDER);
					return true;
				}
				player.teleport(loc); // TODO: folia
			}
			dest.subtract(0, 1, 0);
		}
		return false;
	}

	public boolean containsAnyLiquid(World world, BoundingBox box) {
		int minX = (int)Math.floor(box.getMinX());
		int maxX = (int)Math.ceil(box.getMaxX());
		int minY = (int)Math.floor(box.getMinY());
		int maxY = (int)Math.ceil(box.getMaxY());
		int minZ = (int)Math.floor(box.getMinZ());
		int maxZ = (int)Math.ceil(box.getMaxZ());
		Location location = new Location(world, 0, 0, 0);

		for(int o = minX; o < maxX; ++o) {
			for(int p = minY; p < maxY; ++p) {
				for(int q = minZ; q < maxZ; ++q) {
					Block block = world.getBlockAt(location.set(o, p, q));
					if (block.isLiquid()) {
						return true;
					}
				}
			}
		}

		return false;
	}
}
