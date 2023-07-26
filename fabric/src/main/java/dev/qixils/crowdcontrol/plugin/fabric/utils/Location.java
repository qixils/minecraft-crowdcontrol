package dev.qixils.crowdcontrol.plugin.fabric.utils;

import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Contract;
import org.joml.Vector3d;
import org.joml.Vector3f;

import javax.annotation.CheckReturnValue;

@FieldsAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record Location(ServerLevel level, double x, double y, double z, float yaw, float pitch) {
	public Location(Entity player) {
		this((ServerLevel) player.level(), player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
	}

	public Location(ServerLevel level, double x, double y, double z) {
		this(level, x, y, z, 0, 0);
	}

	public Location(ServerLevel level, BlockPos pos, float yaw, float pitch) {
		this(level, pos.getX(), pos.getY(), pos.getZ(), yaw, pitch);
	}

	public Location(ServerLevel level, BlockPos pos) {
		this(level, pos.getX(), pos.getY(), pos.getZ());
	}

	public BlockPos pos() {
		return BlockPos.containing(x, y, z);
	}

	public Vec3 vec3() {
		return new Vec3(x, y, z);
	}

	public Vector3d vector3d() {
		return new Vector3d(x, y, z);
	}

	public Vector3f vector3f() {
		return new Vector3f((float) x, (float) y, (float) z);
	}

	public void teleportHere(ServerPlayer player) {
		player.teleportTo(level, x, y, z, yaw, pitch);
	}

	@CheckReturnValue
	@Contract("_, _, _ -> new")
	public Location add(double x, double y, double z) {
		return new Location(level, this.x + x, this.y + y, this.z + z, yaw, pitch);
	}

	@CheckReturnValue
	@Contract("-> new")
	public Location atVertCeil() {
		return new Location(level, x, Math.ceil(y), z, yaw, pitch);
	}

	@CheckReturnValue
	@Contract("_ -> new")
	public Location at(BlockPos pos) {
		return new Location(level, pos, yaw, pitch);
	}

	@CheckReturnValue
	@Contract("_, _ -> new")
	public Location withRotation(float yaw, float pitch) {
		return new Location(level, x, y, z, yaw, pitch);
	}

	@CheckReturnValue
	@Contract("_ -> new")
	public Location withRotationOf(Location other) {
		return new Location(level, x, y, z, other.yaw, other.pitch);
	}

	@CheckReturnValue
	@Contract("_, _, _ -> new")
	public Location withPosition(double x, double y, double z) {
		return new Location(level, x, y, z, yaw, pitch);
	}

	@CheckReturnValue
	@Contract("_ -> new")
	public Location withPositionOf(Location other) {
		return new Location(other.level, other.x, other.y, other.z, yaw, pitch);
	}

	public BlockState block() {
		return level.getBlockState(pos());
	}

	public void block(BlockState state) {
		level.setBlockAndUpdate(pos(), state);
	}

	public <T extends ParticleOptions> ParticleEffectBuilder<T> buildParticleEffect(T options) {
		return new ParticleEffectBuilder<>(options).level(level).position(vector3d());
	}

	public Location relative(Direction direction) {
		return new Location(level, x + direction.getStepX(), y + direction.getStepY(), z + direction.getStepZ(), yaw, pitch);
	}
}
