package dev.qixils.crowdcontrol.plugin.mojmap.utils;

import com.mojang.math.Vector3d;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Contract;

import javax.annotation.CheckReturnValue;

public record Location(ServerLevel level, double x, double y, double z, float yaw, float pitch) {
	public Location(Entity player) {
		this((ServerLevel) player.getLevel(), player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
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
		return new BlockPos(x, y, z);
	}

	public Vec3 vec() {
		return new Vec3(x, y, z);
	}

	public Vector3d vector() {
		return new Vector3d(x, y, z);
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

	public BlockState block() {
		return level.getBlockState(pos());
	}

	public void block(BlockState state) {
		level.setBlockAndUpdate(pos(), state);
	}

	public <T extends ParticleOptions> ParticleEffectBuilder<T> buildParticleEffect(T options) {
		return new ParticleEffectBuilder<>(options).level(level).position(vector());
	}
}
