package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.RegionalCommand;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.Position;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.EAT_CHORUS_FRUIT_MAX_RADIUS;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.EAT_CHORUS_FRUIT_MIN_RADIUS;
import static org.bukkit.Tag.REGISTRY_BLOCKS;

@Getter
public class TeleportCommand extends RegionalCommand {
	private final String effectName = "chorus_fruit";
	private final List<String> effectGroups = List.of("walk", "look");

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
		if (isArrayActive(ccPlayer))
			return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Cannot teleport while frozen");
		return null;
	}

	@Override
	protected @NotNull CCEffectResponse buildFailure(@NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "No teleportation destinations were available");
	}

	@Override
	protected CompletableFuture<Boolean> executeRegionallyAsync(@NotNull Player player, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		return CompletableFuture.supplyAsync(() -> {
			Location loc = player.getLocation();
			Vector oldPos = loc.toVector();
			World level = loc.getWorld();
			double x = loc.getX();
			double y = loc.getY();
			double z = loc.getZ();
			if (player.isInsideVehicle())
				player.leaveVehicle();
			// wtf mojang gives us variable names now?
			for (int attempt = 0; attempt < 16; attempt++) {
				double xx = x + nextDoubleOffset();
				double yy = Math.clamp(
					level.getMinHeight(),
					level.getMinHeight() + level.getLogicalHeight() - 1,
					y + nextDoubleOffset()
				);
				double zz = z + nextDoubleOffset();

				// TODO use fixed tag
				Tag<Material> tag = Bukkit.getTag(REGISTRY_BLOCKS, NamespacedKey.minecraft("consumable_does_not_teleport_to"), Material.class);
				if (tag != null && randomTeleport(player, xx, yy, zz, true, tag)) {
					Location newPos = loc.clone().set(xx, yy, zz);
					level.sendGameEvent(player, GameEvent.TELEPORT, oldPos);

					level.playSound(newPos, Sound.ITEM_CHORUS_FRUIT_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.0f);

					BlockPosition origin = Position.block(loc);
					BlockPosition target = Position.block(newPos);
					level.playEffect(loc, Effect.CONSUME_EFFECT_TELEPORT, clampedPackDifferenceInPosition(origin, target, 127, 127, 127));

					player.setFallDistance(0);
					// TODO player.resetCurrentImpulseContext();
					return true;
				}
			}
			return false;
		}, getPlugin().getSyncExecutor());
	}

	public boolean randomTeleport(Player player, double xx, double yy, double zz, boolean showParticles, Tag<Material> avoidanceTag) {
		return this.randomTeleport(player, xx, yy, zz, showParticles, (state) -> avoidanceTag.isTagged(state.getType()));
	}

	public boolean randomTeleport(Player player, double xx, double yy, double zz, boolean showParticles, Predicate<BlockState> isInvalidPosition) {
		double y = yy;
		World level = player.getWorld();
		WorldBorder worldBorder = level.getWorldBorder();
		double size = worldBorder.getSize() / 2;
		BoundingBox borderBox = BoundingBox.of(worldBorder.getCenter(), size, 0, size);
		Vector clamped = new Vector(
			Math.clamp(borderBox.getMinX(), borderBox.getMaxX(), xx),
			yy,
			Math.clamp(borderBox.getMinZ(), borderBox.getMaxZ(), zz)
		);
		Vector pos = clamped.clone();
		int chunkX = (int) Math.floor(pos.getBlockX()) >> 4;
		int chunkZ = (int) Math.floor(pos.getBlockZ()) >> 4;
		if (level.isChunkLoaded(chunkX, chunkZ)) {
			while(pos.getBlockY() > level.getMinHeight()) {
				pos.setY(pos.getY() - 1);
				BlockState state = level.getBlockState(pos);
				if (Tag.ENTITIES_CAN_TELEPORT_TO.isTagged(state.getType())) {
					return this.checkPositionAndTeleport(player, clamped.getX(), y, clamped.getZ(), showParticles, isInvalidPosition, state, level);
				}

				--y;
			}
		}

		return false;
	}

	private boolean checkPositionAndTeleport(Player player, double xx, double y, double zz, boolean showParticles, Predicate<BlockState> isInvalidPosition, BlockState state, World level) {
		if (isInvalidPosition.test(state)) {
			return false;
		}

		BoundingBox base = player.getBoundingBox();
		BoundingBox aabb = BoundingBox.of(new Vector(xx, y, zz), base.getWidthX() / 2, base.getHeight() / 2, base.getWidthZ() / 2);
		if (level.hasCollisionsIn(aabb) || containsAnyLiquid(level, aabb)) {
			return false;
		}
		// TODO: hasInvalidPosition aabb check (i cant be bothered atm)

		// this is always `true` for non-enderman
//		if (!canRandomlyTeleportTo(xx, y, zz)) {
//			return false;
//		}

		Location newLoc = player.getLocation().set(xx, y, zz);
		player.teleport(newLoc); // TODO folia
		if (showParticles) {
			player.sendEntityEffect(EntityEffect.TELEPORT_ENDER, player);
			for (Player nearby : level.getNearbyPlayers(newLoc, 64)) {
				if (nearby.getUniqueId().equals(player.getUniqueId())) continue;
				nearby.sendEntityEffect(EntityEffect.TELEPORT_ENDER, player);
			}
		}

//		if (this instanceof PathfinderMob) {
//			PathfinderMob pathfinderMob = (PathfinderMob)this;
//			pathfinderMob.getNavigation().stop();
//		}

		return true;
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

	private static int packDifferenceInPositionInternal(int xRadius, int yRadius, int zRadius, int xDiff, int yDiff, int zDiff) {
		return (xDiff + xRadius & 255) << 16 | (yDiff + yRadius & 255) << 8 | zDiff + zRadius & 255;
	}

	private static int clampedPackDifferenceInPosition(BlockPosition pos, BlockPosition testPos, int xRadius, int yRadius, int zRadius) {
		int clampedXRadius = Math.clamp(xRadius, 0, 127);
		int clampedYRadius = Math.clamp(yRadius, 0, 127);
		int clampedZRadius = Math.clamp(zRadius, 0, 127);
		int xDiff = Math.clamp(testPos.blockX() - pos.blockX(), -127, 127);
		int yDiff = Math.clamp(testPos.blockY() - pos.blockY(), -127, 127);
		int zDiff = Math.clamp(testPos.blockZ() - pos.blockZ(), -127, 127);
		return packDifferenceInPositionInternal(clampedXRadius, clampedYRadius, clampedZRadius, xDiff, yDiff, zDiff);
	}
}
