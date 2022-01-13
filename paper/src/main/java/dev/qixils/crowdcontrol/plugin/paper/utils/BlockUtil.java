package dev.qixils.crowdcontrol.plugin.paper.utils;

import dev.qixils.crowdcontrol.common.util.AbstractBlockFinder;
import dev.qixils.crowdcontrol.common.util.CommonTags;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

public class BlockUtil {
	public static final MaterialTag FLOWERS = new MaterialTag(CommonTags.FLOWERS);
	public static final MaterialTag STONES_TAG = new MaterialTag(CommonTags.STONES);
	public static final MaterialTag TORCHES = new MaterialTag(CommonTags.TORCHES);

	public static Predicate<Location> SPAWNING_SPACE = location -> location.getBlock().isPassable()
			&& location.clone().add(0, 1, 0).getBlock().isPassable()
			&& location.clone().subtract(0, 1, 0).getBlock().isSolid();

	public static BlockFinder.BlockFinderBuilder blockFinderBuilder() {
		return BlockFinder.builder();
	}

	public static class BlockFinder extends AbstractBlockFinder<Location, Vector, World> {
		private BlockFinder(World origin, List<Vector> positions, Predicate<Location> locationValidator) {
			super(origin, positions, locationValidator);
		}

		public static BlockFinderBuilder builder() {
			return new BlockFinderBuilder();
		}

		@Override
		protected Location getLocation(Vector position) {
			return position.toLocation(origin);
		}

		public static class BlockFinderBuilder extends AbstractBlockFinderBuilder<BlockFinderBuilder, BlockFinder, Location, Vector, World> {

			public @NotNull BlockFinderBuilder origin(Location origin) {
				if (origin == null)
					return this;
				return originPos(origin.toVector()).originWorld(origin.getWorld());
			}

			@Override
			protected int getFloorX(@NotNull Vector pos) {
				return pos.getBlockX();
			}

			@Override
			protected int getFloorY(@NotNull Vector pos) {
				return pos.getBlockY();
			}

			@Override
			protected int getFloorZ(@NotNull Vector pos) {
				return pos.getBlockZ();
			}

			@Override
			protected @NotNull Vector createVector(int x, int y, int z) {
				return new Vector(x, y, z);
			}

			public BlockFinder build() {
				List<Vector> positions = preBuildDataInit();
				return new BlockFinder(originWorld, positions, locationValidator);
			}
		}
	}
}
