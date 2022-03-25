package dev.qixils.crowdcontrol.plugin.mojmap;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

public record Location(ServerLevel level, double x, double y, double z, float yaw, float pitch) {
	public Location(ServerPlayer player) {
		this(player.getLevel(), player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
	}

	public void setLocation(ServerPlayer player) {
		player.teleportTo(level, x, y, z, yaw, pitch);
	}

	public Location add(double x, double y, double z) {
		return new Location(level, this.x + x, this.y + y, this.z + z, yaw, pitch);
	}

	public void setBlock(BlockState state) {
		level.setBlockAndUpdate(new BlockPos(x, y, z), state);
	}
}
