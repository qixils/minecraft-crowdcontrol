package dev.qixils.crowdcontrol.plugin.fabric.utils;

import dev.qixils.crowdcontrol.common.util.AbstractBlockFinder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

public final class BlockFinder extends AbstractBlockFinder<Location, BlockPos, ServerLevel> {
	public static final Predicate<Location> SPAWNING_SPACE = location ->
			isPassable(location)
					&& isPassable(location.add(0, 1, 0).block())
					&& isSolid(location.add(0, -1, 0).block());

	// helper methods

	public static boolean isPassable(Material material) {
		return !material.blocksMotion();
	}

	public static boolean isPassable(BlockState block) {
		return isPassable(block.getMaterial());
	}

	public static boolean isPassable(Location location) {
		return isPassable(location.block());
	}

	public static boolean isSolid(BlockState block) {
		return block.getMaterial().isSolid();
	}

	public static boolean isSolid(Location location) {
		return isSolid(location.block());
	}

	public static boolean isReplaceable(Material material) {
		return material.isReplaceable();
	}

	public static boolean isReplaceable(BlockState block) {
		return isReplaceable(block.getMaterial());
	}

	public static boolean isReplaceable(Location location) {
		return isReplaceable(location.block());
	}

	// actual class stuff

	private BlockFinder(ServerLevel origin, List<BlockPos> positions, Predicate<Location> locationValidator) {
		super(origin, positions, locationValidator);
	}

	public static BlockFinderBuilder builder() {
		return new BlockFinderBuilder();
	}

	@Override
	protected Location getLocation(BlockPos position) {
		return new Location(origin, position);
	}

	public static final class BlockFinderBuilder extends AbstractBlockFinderBuilder<BlockFinderBuilder, BlockFinder, Location, BlockPos, ServerLevel> {

		public @NotNull BlockFinderBuilder origin(Location origin) {
			if (origin == null)
				return this;
			return originPos(origin.pos()).originWorld(origin.level());
		}

		public @NotNull BlockFinderBuilder origin(Entity entity) {
			if (entity == null)
				return this;
			return origin(new Location(entity));
		}

		@Override
		protected int getFloorX(@NotNull BlockPos pos) {
			return pos.getX();
		}

		@Override
		protected int getFloorY(@NotNull BlockPos pos) {
			return pos.getY();
		}

		@Override
		protected int getFloorZ(@NotNull BlockPos pos) {
			return pos.getZ();
		}

		@Override
		protected @NotNull BlockPos createVector(int x, int y, int z) {
			return new BlockPos(x, y, z);
		}

		public BlockFinder build() {
			List<BlockPos> positions = preBuildDataInit();
			return new BlockFinder(originWorld, positions, locationValidator);
		}
	}
}
