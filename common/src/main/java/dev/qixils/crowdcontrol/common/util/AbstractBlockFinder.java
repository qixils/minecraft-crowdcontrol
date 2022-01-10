package dev.qixils.crowdcontrol.common.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public abstract class AbstractBlockFinder<L, V, W> {
	protected final W origin;
	protected final List<V> positions;
	protected final Predicate<L> locationValidator;

	protected AbstractBlockFinder(W origin, List<V> positions, Predicate<L> locationValidator) {
		this.origin = origin;
		this.positions = positions;
		this.locationValidator = locationValidator;
	}

	protected abstract L getLocation(V position);

	@Nullable
	public L next() {
		if (positions.isEmpty())
			return null;
		L location = getLocation(positions.remove(0));
		if (locationValidator.test(location))
			return location;
		return next();
	}

	@NotNull
	public List<L> getAll() {
		List<L> list = new ArrayList<>();
		L next = next();
		while (next != null) {
			list.add(next);
			next = next();
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	public static abstract class AbstractBlockFinderBuilder<B extends AbstractBlockFinderBuilder<B, F, L, V, W>, F extends AbstractBlockFinder<L, V, W>, L, V, W> {
		protected W originWorld;
		protected V originPos;
		protected int maxRadius = 0;
		protected int minRadius = 0;
		protected boolean shuffleLocations = true;
		protected Predicate<L> locationValidator;

		protected abstract Predicate<L> defaultPredicate();

		public B maxRadius(int maxRadius) {
			this.maxRadius = maxRadius;
			return (B) this;
		}

		public B minRadius(int minRadius) {
			this.minRadius = minRadius;
			return (B) this;
		}

		public B shuffleLocations(boolean shuffleLocations) {
			this.shuffleLocations = shuffleLocations;
			return (B) this;
		}

		public B originPos(V originPos) {
			this.originPos = originPos;
			return (B) this;
		}

		public B originWorld(W originWorld) {
			this.originWorld = originWorld;
			return (B) this;
		}

		public abstract B origin(L origin);

		public B locationValidator(Predicate<L> validator) {
			this.locationValidator = validator;
			return (B) this;
		}

		protected abstract int getFloorX(V pos);

		protected abstract int getFloorY(V pos);

		protected abstract int getFloorZ(V pos);

		protected abstract V createVector(int x, int y, int z);

		protected List<V> preBuildDataInit() {
			if (maxRadius <= 0)
				throw new IllegalStateException("maxRadius must be greater than 0");
			if (originWorld == null)
				throw new IllegalStateException("originWorld is not set");
			if (locationValidator == null)
				locationValidator = defaultPredicate();

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

		public abstract AbstractBlockFinder<L, V, W> build();
	}


}
