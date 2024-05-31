package dev.qixils.crowdcontrol.plugin.sponge8.utils;

import dev.qixils.crowdcontrol.common.util.AbstractBlockFinder;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.MatterType;
import org.spongepowered.api.data.type.MatterTypes;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3i;

import java.util.List;
import java.util.function.Predicate;

public final class BlockFinder extends AbstractBlockFinder<ServerLocation, Vector3i, ServerWorld> {
	public static final Predicate<ServerLocation> SPAWNING_SPACE = location ->
			isPassable(location.block())
					&& !isLiquid(location.block())
					&& isPassable(location.add(0, 1, 0).block())
				    && !isLiquid(location.add(0, 1, 0).block())
					&& isSolid(location.sub(0, 1, 0).block());

	private BlockFinder(ServerWorld origin, List<Vector3i> positions, Predicate<ServerLocation> locationValidator) {
		super(origin, positions, locationValidator);
	}

	public static boolean isProperty(BlockState block, Key<Value<Boolean>> propertyKey, boolean def) {
		return block.get(propertyKey).orElse(def);
	}

	public static boolean isMatter(BlockState block, MatterType matter) {
		return block.get(Keys.MATTER_TYPE).map(actual -> actual.equals(matter)).orElse(false);
	}

	public static boolean isLiquid(BlockState block) {
		return isMatter(block, MatterTypes.LIQUID.get());
	}

	public static boolean isAir(BlockState block) {
		return isMatter(block, MatterTypes.GAS.get());
	}

	public static boolean isPassable(BlockState block) {
		return isProperty(block, Keys.IS_PASSABLE, false);
	}

	public static boolean isSolid(BlockState block) {
		return isProperty(block, Keys.IS_SOLID, true);
	}

	public static boolean isSolid(BlockType blockType) {
		return isSolid(blockType.defaultState());
	}

	public static boolean isReplaceable(BlockState block) {
		return isProperty(block, Keys.IS_REPLACEABLE, false);
	}

	public static BlockFinderBuilder builder() {
		return new BlockFinderBuilder();
	}

	@Override
	protected ServerLocation getLocation(Vector3i position) {
		return origin.location(position);
	}

	public static final class BlockFinderBuilder extends AbstractBlockFinderBuilder<BlockFinderBuilder, BlockFinder, ServerLocation, Vector3i, ServerWorld> {

		public @NotNull BlockFinderBuilder origin(ServerLocation origin) {
			if (origin == null)
				return this;
			return originPos(origin.position().toInt()).originWorld(origin.world());
		}

		@Override
		protected int getFloorX(@NotNull Vector3i pos) {
			return pos.x();
		}

		@Override
		protected int getFloorY(@NotNull Vector3i pos) {
			return pos.y();
		}

		@Override
		protected int getFloorZ(@NotNull Vector3i pos) {
			return pos.z();
		}

		@Override
		protected @NotNull Vector3i createVector(int x, int y, int z) {
			return new Vector3i(x, y, z);
		}

		public BlockFinder build() {
			List<Vector3i> positions = preBuildDataInit();
			return new BlockFinder(originWorld, positions, locationValidator);
		}
	}
}
