package dev.qixils.crowdcontrol.plugin.fabric.client.lidar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Hides the world entirely, then fires a spray of rays from the player every tick and leaves a dot
 * wherever one lands, so the surroundings only come back as a slowly accumulating point cloud.
 * <p>
 * Most rays cluster around the crosshair, so the player has to sweep their view around to map anything out;
 * a few stray ones land randomly across the screen.
 * <p>
 * Rays pass straight through anything without a collision box, like grass and flowers,
 * but do stop at the surface of liquids because it's fun :)
 */
public final class LidarEffect {
	private static final Logger LOGGER = LoggerFactory.getLogger("CrowdControl/LiDAR");

	/** How far a ray travels before giving up. */
	private static final double RANGE = 64;
	/** Rays fired around the crosshair each tick. */
	private static final int FOCUSED_RAYS_PER_TICK = 56;
	/** Rays fired randomly on screen each tick. */
	private static final int STRAY_RAYS_PER_TICK = 6;
	/** Standard deviation of the rays around the crosshair, as a tangent of the angle off it. */
	private static final double FOCUS_SPREAD = Math.tan(Math.toRadians(8));
	/** Dots are re-measured from scratch if the player strays this far, before float precision suffers. */
	private static final double MAX_ANCHOR_DISTANCE = 2048;

	/** Distances and colors of the gradient that terrain dots are tinted by, nearest first. */
	private static final float[] GRADIENT_DISTANCES = {0, 12, 28, 48};
	private static final int[] GRADIENT_COLORS = {0xFFFFF4D6, 0xFF6EFFD6, 0xFF3C8CFF, 0xFF783CC8};
	private static final int HAZARD_COLOR = 0xFFFF5A14;
	private static final int ENEMY_COLOR = 0xFFFF3C3C;
	private static final int CREATURE_COLOR = 0xFFFFC440;

	private final LidarRenderer renderer = new LidarRenderer();
	private final RandomSource random = RandomSource.create();

	// set by the packet handler, which may not be on the main thread
	private volatile long activeUntil;

	// everything below is only touched on the main thread, which is also the render thread
	private @Nullable ClientLevel level;
	private @Nullable Vec3 anchor;

	/**
	 * Starts the effect, or extends it if it is already running.
	 *
	 * @param duration how long until the effect turns itself back off
	 */
	public void start(@NotNull Duration duration) {
		activeUntil = Util.getMillis() + duration.toMillis();
	}

	/**
	 * Stops the effect.
	 */
	public void stop() {
		activeUntil = 0;
	}

	private boolean isActive() {
		return Util.getMillis() < activeUntil;
	}

	/**
	 * Whether the world is currently being replaced by the point cloud.
	 *
	 * @return whether the effect is visible
	 */
	public boolean isVisible() {
		return anchor != null && isActive();
	}

	/**
	 * Fires this tick's rays.
	 *
	 * @param minecraft client
	 */
	public void tick(@NotNull Minecraft minecraft) {
		ClientLevel level = minecraft.level;
		LocalPlayer player = minecraft.player;
		if (level == null || player == null) {
			// whatever server asked for this is gone
			stop();
			reset();
			return;
		}
		if (!isActive()) {
			reset();
			return;
		}

		if (level != this.level || anchor == null || !anchor.closerThan(player.position(), MAX_ANCHOR_DISTANCE)) {
			reset();
			this.level = level;
			this.anchor = Vec3.atLowerCornerOf(player.blockPosition());
		}

		if (minecraft.isPaused()) return;
		scan(minecraft, level, player, anchor);
	}

	/**
	 * Replaces the level that was just rendered with the point cloud, if the effect is active.
	 *
	 * @param gameRenderer renderer whose level pass just finished
	 */
	public void render(@NotNull GameRenderer gameRenderer) {
		Vec3 anchor = this.anchor;
		if (anchor == null || !isActive()) return;
		try {
			renderer.render(gameRenderer, anchor, Util.getMillis());
		} catch (RuntimeException e) {
			// rather than failing the same way every single frame
			LOGGER.error("Failed to render the LiDAR effect; turning it off", e);
			stop();
			reset();
		}
	}

	private void reset() {
		if (level == null && anchor == null) return;
		level = null;
		anchor = null;
		renderer.clear();
	}

	private void scan(Minecraft minecraft, ClientLevel level, LocalPlayer player, Vec3 anchor) {
		Vec3 eye = player.getEyePosition();
		Vec3 forward = player.getViewVector(1);
		Vec3 right = forward.cross(new Vec3(0, 1, 0));
		right = right.lengthSqr() < 1.0E-6 ? new Vec3(1, 0, 0) : right.normalize();
		Vec3 up = right.cross(forward);

		double tanHalfHeight = Math.tan(Math.toRadians(minecraft.options.fov().get()) / 2);
		double aspect = (double) minecraft.getWindow().getWidth() / Math.max(1, minecraft.getWindow().getHeight());
		double tanHalfWidth = tanHalfHeight * aspect;

		// a ray that starts inside a fluid would otherwise stop dead at the player's own eyes
		ClipContext.Fluid fluid = player.isEyeInFluid(FluidTags.WATER) || player.isEyeInFluid(FluidTags.LAVA)
			? ClipContext.Fluid.NONE
			: ClipContext.Fluid.ANY;
		Entity vehicle = player.getRootVehicle();
		List<Entity> entities = level.getEntities(player, player.getBoundingBox().inflate(RANGE), entity ->
			entity.isPickable() && !entity.isSpectator() && !entity.isInvisibleTo(player) && entity.getRootVehicle() != vehicle);

		long now = Util.getMillis();
		for (int i = 0; i < FOCUSED_RAYS_PER_TICK + STRAY_RAYS_PER_TICK; i++) {
			double x, y;
			if (i < FOCUSED_RAYS_PER_TICK) {
				x = random.nextGaussian() * FOCUS_SPREAD;
				y = random.nextGaussian() * FOCUS_SPREAD;
			} else {
				x = (random.nextDouble() * 2 - 1) * tanHalfWidth;
				y = (random.nextDouble() * 2 - 1) * tanHalfHeight;
			}
			Vec3 direction = forward.add(right.scale(x)).add(up.scale(y)).normalize();
			fire(level, player, eye, direction, fluid, entities, anchor, now);
		}
	}

	private void fire(ClientLevel level, LocalPlayer player, Vec3 eye, Vec3 direction, ClipContext.Fluid fluid, List<Entity> entities, Vec3 anchor, long now) {
		Vec3 end = eye.add(direction.scale(RANGE));
		BlockHitResult blockHit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, fluid, player));
		boolean hitBlock = blockHit.getType() != HitResult.Type.MISS && !blockHit.isInside();
		Vec3 limit = hitBlock ? blockHit.getLocation() : end;

		// an entity in front of whatever block the ray reached gets hit instead
		Entity hitEntity = null;
		Vec3 entityHit = null;
		double nearest = eye.distanceToSqr(limit);
		for (Entity entity : entities) {
			AABB box = entity.getBoundingBox().inflate(entity.getPickRadius());
			Optional<Vec3> clip = box.clip(eye, limit);
			if (clip.isEmpty()) continue;
			double distance = eye.distanceToSqr(clip.get());
			if (distance >= nearest) continue;
			nearest = distance;
			hitEntity = entity;
			entityHit = clip.get();
		}

		if (hitEntity != null) {
			int color = hitEntity instanceof Enemy ? ENEMY_COLOR : CREATURE_COLOR;
			renderer.addEcho(dot(entityHit, direction.reverse(), Math.sqrt(nearest), color, anchor), now);
		} else if (hitBlock) {
			double distance = Math.sqrt(nearest);
			Vec3 normal = blockHit.getDirection().getUnitVec3();
			renderer.addDot(dot(blockHit.getLocation(), normal, distance, blockColor(level, blockHit.getBlockPos(), distance), anchor));
		}
	}

	private LidarDot dot(Vec3 location, Vec3 normal, double distance, int color, Vec3 anchor) {
		// lift the dot off the surface just enough that it doesn't z-fight with it
		double lift = 0.01 + distance * 0.0005;
		// sized to stay a few pixels across when seen from where it was scanned
		float half = (float) Mth.clamp(distance * 0.002, 0.004, 0.25);
		// a little variation in brightness keeps the cloud from looking like flat paint
		color = ARGB.scaleRGB(color, 0.8F + random.nextFloat() * 0.2F);
		return new LidarDot(
			(float) (location.x + normal.x * lift - anchor.x),
			(float) (location.y + normal.y * lift - anchor.y),
			(float) (location.z + normal.z * lift - anchor.z),
			(float) normal.x, (float) normal.y, (float) normal.z,
			half,
			color
		);
	}

	private static int blockColor(ClientLevel level, BlockPos pos, double distance) {
		BlockState state = level.getBlockState(pos);
		// let them kinda see hazards
		if (state.getFluidState().is(FluidTags.LAVA) || state.is(BlockTags.CAMPFIRES) || state.is(Blocks.MAGMA_BLOCK))
			return HAZARD_COLOR;

		for (int i = 1; i < GRADIENT_DISTANCES.length; i++) {
			if (distance > GRADIENT_DISTANCES[i]) continue;
			float progress = (float) (distance - GRADIENT_DISTANCES[i - 1]) / (GRADIENT_DISTANCES[i] - GRADIENT_DISTANCES[i - 1]);
			return ARGB.srgbLerp(progress, GRADIENT_COLORS[i - 1], GRADIENT_COLORS[i]);
		}
		return GRADIENT_COLORS[GRADIENT_COLORS.length - 1];
	}
}
