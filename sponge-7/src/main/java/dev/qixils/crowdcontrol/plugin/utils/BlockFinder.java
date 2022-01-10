package dev.qixils.crowdcontrol.plugin.utils;

import com.flowpowered.math.vector.Vector3d;
import dev.qixils.crowdcontrol.common.util.RandomUtil;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.property.BooleanProperty;
import org.spongepowered.api.data.property.block.PassableProperty;
import org.spongepowered.api.data.property.block.ReplaceableProperty;
import org.spongepowered.api.data.property.block.SolidCubeProperty;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@AllArgsConstructor
public class BlockFinder {
	public static Predicate<Location<World>> SPAWNING_SPACE = location ->
			isPassable(location.getBlock())
					&& isPassable(location.add(0, 1, 0).getBlock())
					&& isSolid(location.sub(0, 1, 0).getBlock());
	private final World origin;
	private final List<Vector3d> locations;
	private final Predicate<Location<World>> locationValidator;

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

	@Nullable
	public Location<World> next() {
		if (locations.isEmpty())
			return null;
		Location<World> location = origin.getLocation(locations.remove(0));
		if (locationValidator.test(location))
			return location;
		return next();
	}

	@NotNull
	public List<Location<World>> getAll() {
		List<Location<World>> list = new ArrayList<>();
		Location<World> next = next();
		while (next != null) {
			list.add(next);
			next = next();
		}
		return list;
	}

	public static class BlockFinderBuilder {
		private static final Predicate<Location<World>> TRUE = $ -> true;
		private World origin = null;
		private Vector3d originPos = null;
		private Integer maxRadius = null;
		private int minRadius = 0;
		private boolean shuffleLocations = true;
		private Predicate<Location<World>> locationValidator;

		public BlockFinderBuilder maxRadius(int maxRadius) {
			this.maxRadius = maxRadius;
			return this;
		}

		public BlockFinderBuilder minRadius(int minRadius) {
			this.minRadius = minRadius;
			return this;
		}

		public BlockFinderBuilder shuffleLocations(boolean shuffleLocations) {
			this.shuffleLocations = shuffleLocations;
			return this;
		}

		public BlockFinderBuilder originPos(Vector3d originPos) {
			this.originPos = originPos;
			return this;
		}

		public BlockFinderBuilder origin(World origin) {
			this.origin = origin;
			return this;
		}

		public BlockFinderBuilder origin(Location<World> origin) {
			return originPos(origin.getPosition()).origin(origin.getExtent());
		}

		public BlockFinderBuilder locationValidator(Predicate<Location<World>> validator) {
			this.locationValidator = validator;
			return this;
		}

		public BlockFinder build() {
			if (maxRadius == null)
				throw new IllegalStateException("maxRadius is not set");
			if (origin == null)
				throw new IllegalStateException("origin is not set");
			if (locationValidator == null)
				locationValidator = TRUE;

			List<Vector3d> locations = new ArrayList<>((int) (Math.pow(maxRadius, 3) - Math.pow(minRadius, 3)));

			int origX = originPos.getFloorX();
			int origY = originPos.getFloorY();
			int origZ = originPos.getFloorZ();
			for (int x = -maxRadius; x <= maxRadius; x++) {
				if (Math.abs(x) < minRadius)
					continue;
				for (int y = -maxRadius; y <= maxRadius; y++) {
					if (Math.abs(y) < minRadius)
						continue;
					for (int z = -maxRadius; z <= maxRadius; z++) {
						if (Math.abs(z) < minRadius)
							continue;
						locations.add(new Vector3d(origX + x, origY + y, origZ + z));
					}
				}
			}

			if (this.shuffleLocations)
				Collections.shuffle(locations, RandomUtil.RNG);

			return new BlockFinder(origin, locations, locationValidator);
		}
	}
}
