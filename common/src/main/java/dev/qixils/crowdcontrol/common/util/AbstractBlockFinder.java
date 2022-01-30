package dev.qixils.crowdcontrol.common.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * Utility class for fetching (random) nearby blocks that match a certain criteria.
 *
 * @param <L> platform's location class
 * @param <V> platform's 3D integer vector class
 * @param <W> platform's world class
 */
public abstract class AbstractBlockFinder<L, V, W> {

	/**
	 * The world in which this operation is occurring.
	 */
	protected final W origin;
	/**
	 * List of remaining potentially valid coordinates.
	 */
	protected final List<V> positions;
	/**
	 * Predicate to validate coordinates from the {@code positions} list.
	 */
	protected final Predicate<L> locationValidator;

	private final List<L> allLocations = new ArrayList<>();

	/**
	 * Creates a new block finder given the origin world, a list of potentially valid coordinates,
	 * and a predicate to validate these coordinates.
	 *
	 * @param origin            world in which this operation is occurring
	 * @param positions         list of potentially valid coordinates
	 * @param locationValidator predicate to validate items from the aforementioned list of
	 *                          coordinates
	 */
	protected AbstractBlockFinder(@NotNull W origin,
								  @NotNull List<V> positions,
								  @NotNull Predicate<L> locationValidator) {
		this.origin = origin;
		this.positions = positions;
		this.locationValidator = locationValidator;
	}

	/**
	 * Gets the corresponding location for a coordinate.
	 *
	 * @param position a set of coordinates
	 * @return a location (consisting of the {@code world} and the provided {@code position})
	 */
	protected abstract L getLocation(V position);

	/**
	 * Gets the next valid location or null if the list of potentially valid coordinates have been
	 * exhausted.
	 *
	 * @return next valid location or null
	 */
	@Nullable
	public L next() {
		if (positions.isEmpty())
			return null;
		L location = getLocation(positions.remove(0));
		if (locationValidator.test(location)) {
			allLocations.add(location);
			return location;
		}
		return next();
	}

	/**
	 * Gets every available valid location.
	 *
	 * @return every valid location
	 */
	@NotNull
	public List<L> getAll() {
		L next = next();
		while (next != null) {
			allLocations.add(next);
			next = next();
		}
		return Collections.unmodifiableList(allLocations);
	}

	/**
	 * A builder for {@link AbstractBlockFinder}.
	 * This builder creates the list of positions given a central blocks and a radius of blocks to
	 * search.
	 *
	 * @param <B> builder class
	 * @param <F> block finder class
	 * @param <L> platform's location class
	 * @param <V> platform's 3D integer vector class
	 * @param <W> platform's world class
	 */
	@SuppressWarnings("unchecked")
	public static abstract class AbstractBlockFinderBuilder<B extends AbstractBlockFinderBuilder<B, F, L, V, W>, F extends AbstractBlockFinder<L, V, W>, L, V, W> {
		/**
		 * The world in which this operation is occurring.
		 */
		protected W originWorld;
		/**
		 * The center block for radius-based operation.
		 */
		protected V originPos;
		/**
		 * The maximum (rectangular) radius from the center block to search for additional blocks in
		 * the X, Y, and Z directions.
		 */
		protected int maxRadius = 0;
		/**
		 * The minimum (rectangular) radius from the center block to search for additional blocks in
		 * the X, Y, and Z directions.
		 */
		protected int minRadius = 0;
		/**
		 * Whether to {@link Collections#shuffle(List) shuffle} the generated list of block
		 * coordinates.
		 */
		protected boolean shuffleLocations = true;
		/**
		 * Predicate to validate coordinates from the resulting {@code positions} list.
		 */
		protected Predicate<L> locationValidator;

		/**
		 * Sets the maximum (rectangular) radius from the center block to search for additional
		 * blocks in the X, Y, and Z directions.
		 *
		 * @param maxRadius maximum (rectangular) radius
		 * @return this builder
		 */
		@NotNull
		@Contract("_ -> this")
		public B maxRadius(@Range(from = 0, to = Long.MAX_VALUE) int maxRadius) {
			this.maxRadius = maxRadius;
			return (B) this;
		}

		/**
		 * Sets the minimum (rectangular) radius from the center block to search for additional
		 * blocks in the X, Y, and Z directions.
		 *
		 * @param minRadius minimum (rectangular) radius
		 * @return this builder
		 */
		@NotNull
		@Contract("_ -> this")
		public B minRadius(@Range(from = 0, to = Long.MAX_VALUE) int minRadius) {
			this.minRadius = minRadius;
			return (B) this;
		}

		/**
		 * Determines whether the resulting list of positions should be
		 * {@link Collections#shuffle(List) shuffled}.
		 *
		 * @param shuffleLocations whether positions should be shuffled
		 * @return this builder
		 */
		@NotNull
		@Contract("_ -> this")
		public B shuffleLocations(boolean shuffleLocations) {
			this.shuffleLocations = shuffleLocations;
			return (B) this;
		}

		/**
		 * Sets the center block to generate coordinates around.
		 *
		 * @param originPos center block
		 * @return this builder
		 */
		@NotNull
		@Contract("_ -> this")
		public B originPos(@Nullable V originPos) {
			this.originPos = originPos;
			return (B) this;
		}

		/**
		 * Sets the world to generate locations in.
		 *
		 * @param originWorld world to generate locations in
		 * @return this builder
		 */
		@NotNull
		@Contract("_ -> this")
		public B originWorld(@Nullable W originWorld) {
			this.originWorld = originWorld;
			return (B) this;
		}

		/**
		 * Sets the center block and world to generate locations in given a location.
		 *
		 * @param origin central location
		 * @return this builder
		 */
		@NotNull
		@Contract("_ -> this")
		public abstract B origin(@Nullable L origin);

		/**
		 * Sets the predicate used to validate generated locations.
		 * A returned value of {@code false} will result in the location being skipped from
		 * {@link AbstractBlockFinder#next()}.
		 *
		 * @param validator location validator
		 * @return this builder
		 */
		@NotNull
		@Contract("_ -> this")
		public B locationValidator(@Nullable Predicate<@NotNull L> validator) {
			this.locationValidator = validator;
			return (B) this;
		}

		/**
		 * Gets the floored (block) X-coordinate from a vector.
		 *
		 * @param pos vector coordinates
		 * @return this builder
		 */
		protected abstract int getFloorX(@NotNull V pos);

		/**
		 * Gets the floored (block) Y-coordinate from a vector.
		 *
		 * @param pos vector coordinates
		 * @return this builder
		 */
		protected abstract int getFloorY(@NotNull V pos);

		/**
		 * Gets the floored (block) Z-coordinate from a vector.
		 *
		 * @param pos vector coordinates
		 * @return this builder
		 */
		protected abstract int getFloorZ(@NotNull V pos);

		/**
		 * Creates a 3D integer vector given a set of coordinates.
		 *
		 * @param x x-coordinate
		 * @param y y-coordinate
		 * @param z z-coordinate
		 * @return 3D integer vector
		 */
		@NotNull
		protected abstract V createVector(int x, int y, int z);

		/**
		 * Initializes the list of locations and some optional variables.
		 *
		 * @return list of locations for use in {@link #build()}
		 * @throws IllegalStateException if maxRadius or originWorld are unset
		 */
		@NotNull
		protected List<V> preBuildDataInit() throws IllegalStateException {
			if (maxRadius <= 0)
				throw new IllegalStateException("maxRadius must be greater than 0");
			if (originWorld == null)
				throw new IllegalStateException("originWorld is not set");
			if (locationValidator == null)
				locationValidator = $ -> true;

			List<V> locations = new ArrayList<>((int) (Math.pow(maxRadius, 3) - Math.pow(minRadius, 3)));

			int origX = getFloorX(originPos);
			int origY = getFloorY(originPos);
			int origZ = getFloorZ(originPos);
			for (int x = -maxRadius; x <= maxRadius; x++) {
				if (Math.abs(x) < minRadius)
					continue;
				for (int y = -maxRadius; y <= maxRadius; y++) {
					if (Math.abs(y) < minRadius)
						continue;
					for (int z = -maxRadius; z <= maxRadius; z++) {
						if (Math.abs(z) < minRadius)
							continue;
						locations.add(createVector(origX + x, origY + y, origZ + z));
					}
				}
			}

			if (this.shuffleLocations)
				Collections.shuffle(locations, RandomUtil.RNG);

			return locations;
		}

		/**
		 * Builds a new {@link AbstractBlockFinder}.
		 *
		 * @return new {@link AbstractBlockFinder}
		 * @throws IllegalStateException if maxRadius or originWorld are unset
		 */
		public abstract AbstractBlockFinder<L, V, W> build() throws IllegalStateException;
	}


}
