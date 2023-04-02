package dev.qixils.crowdcontrol.plugin.fabric.utils;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.annotation.FieldsAreNonnullByDefault;
import net.minecraft.util.annotation.MethodsReturnNonnullByDefault;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Contract;
import org.joml.Vector3d;
import org.joml.Vector3f;

import javax.annotation.CheckReturnValue;

@FieldsAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record Location(ServerWorld level, double x, double y, double z, float yaw, float pitch) {
	public Location(Entity player) {
		this((ServerWorld) player.world, player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
	}

	public Location(ServerWorld level, double x, double y, double z) {
		this(level, x, y, z, 0, 0);
	}

	public Location(ServerWorld level, BlockPos pos, float yaw, float pitch) {
		this(level, pos.getX(), pos.getY(), pos.getZ(), yaw, pitch);
	}

	public Location(ServerWorld level, BlockPos pos) {
		this(level, pos.getX(), pos.getY(), pos.getZ());
	}

	public BlockPos pos() {
		return BlockPos.ofFloored(x, y, z);
	}

	public Vec3d vec3() {
		return new Vec3d(x, y, z);
	}

	public Vector3d vector3d() {
		return new Vector3d(x, y, z);
	}

	public Vector3f vector3f() {
		return new Vector3f((float) x, (float) y, (float) z);
	}

	public void teleportHere(ServerPlayerEntity player) {
		player.teleport(level, x, y, z, yaw, pitch);
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
		level.setBlockState(pos(), state);
	}

	public <T extends ParticleEffect> ParticleEffectBuilder<T> buildParticleEffect(T options) {
		return new ParticleEffectBuilder<>(options).level(level).position(vector3d());
	}

	public Location relative(Direction direction) {
		return new Location(level, x + direction.getOffsetX(), y + direction.getOffsetY(), z + direction.getOffsetZ(), yaw, pitch);
	}
}
