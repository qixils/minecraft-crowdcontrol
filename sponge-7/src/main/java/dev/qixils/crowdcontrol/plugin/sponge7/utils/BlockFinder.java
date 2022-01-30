package dev.qixils.crowdcontrol.plugin.sponge7.utils;

import com.flowpowered.math.vector.Vector3i;
import dev.qixils.crowdcontrol.common.util.AbstractBlockFinder;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.property.BooleanProperty;
import org.spongepowered.api.data.property.block.PassableProperty;
import org.spongepowered.api.data.property.block.ReplaceableProperty;
import org.spongepowered.api.data.property.block.SolidCubeProperty;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public final class BlockFinder extends AbstractBlockFinder<Location<World>, Vector3i, World> {
	public static Predicate<Location<World>> SPAWNING_SPACE = location ->
			isPassable(location.getBlock())
					&& isPassable(location.add(0, 1, 0).getBlock())
					&& isSolid(location.sub(0, 1, 0).getBlock());

	private BlockFinder(World origin, List<Vector3i> positions, Predicate<Location<World>> locationValidator) {
		super(origin, positions, locationValidator);
	}

	public static boolean isProperty(BlockState block, Class<? extends BooleanProperty> propertyClass, boolean def) {
		Optional<? extends BooleanProperty> optProperty = block.getProperty(propertyClass);
		if (!optProperty.isPresent()) {
			optProperty = block.getType().getProperty(propertyClass);
			if (!optProperty.isPresent())
				return def;
		}
		Boolean property = optProperty.get().getValue();
		if (property == null)
			return def;
		return property;
	}

	public static boolean isPassable(BlockState block) {
		return isProperty(block, PassableProperty.class, false);
	}

	public static boolean isSolid(BlockState block) {
		return isProperty(block, SolidCubeProperty.class, true);
	}

	public static boolean isReplaceable(BlockState block) {
		return isProperty(block, ReplaceableProperty.class, false);
	}

	public static BlockFinderBuilder builder() {
		return new BlockFinderBuilder();
	}

	@Override
	protected Location<World> getLocation(Vector3i position) {
		return origin.getLocation(position);
	}

	public static final class BlockFinderBuilder extends AbstractBlockFinderBuilder<BlockFinderBuilder, BlockFinder, Location<World>, Vector3i, World> {

		public @NotNull BlockFinderBuilder origin(Location<World> origin) {
			if (origin == null)
				return this;
			return originPos(origin.getPosition().toInt()).originWorld(origin.getExtent());
		}

		@Override
		protected int getFloorX(@NotNull Vector3i pos) {
			return pos.getX();
		}

		@Override
		protected int getFloorY(@NotNull Vector3i pos) {
			return pos.getY();
		}

		@Override
		protected int getFloorZ(@NotNull Vector3i pos) {
			return pos.getZ();
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
