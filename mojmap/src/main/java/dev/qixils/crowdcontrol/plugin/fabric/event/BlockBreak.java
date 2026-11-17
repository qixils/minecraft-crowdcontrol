package dev.qixils.crowdcontrol.plugin.fabric.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true, chain = false)
public final class BlockBreak implements CancellableEvent {
	private final ServerPlayer player;
	private final ServerLevel level;
	private final BlockPos blockPos;
	private final BlockState blockState;
	private final Block block;
	private boolean cancelled = false;
}
