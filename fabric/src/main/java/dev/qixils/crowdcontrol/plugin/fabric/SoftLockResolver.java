package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.common.SoftLockObserver;
import dev.qixils.crowdcontrol.plugin.fabric.event.Death;
import dev.qixils.crowdcontrol.plugin.fabric.event.Listener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.List;

@ParametersAreNonnullByDefault
public final class SoftLockResolver extends SoftLockObserver<ServerPlayerEntity> {

	private final @NotNull Collection<Block> dangerousBlocks = List.of(
			Blocks.LAVA,
			Blocks.FIRE,
			Blocks.WITHER_ROSE
	);
	private final @NotNull TargetPredicate conditions = TargetPredicate.createNonAttackable()
			.ignoreVisibility()
			.ignoreDistanceScalingFactor()
			.setBaseMaxDistance(SEARCH_HORIZ);

	/**
	 * Initializes the observer.
	 *
	 * @param plugin The plugin instance which provides player UUIDs.
	 */
	public SoftLockResolver(FabricCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public void onSoftLock(ServerPlayerEntity player) {
		// kill nearby monsters
		for (MobEntity entity : player.getWorld().getTargets(
				MobEntity.class,
				conditions,
				player,
				Box.of(player.getPos(), SEARCH_HORIZ*2, SEARCH_VERT*2, SEARCH_HORIZ*2)
		)) {
			if (entity instanceof Monster)
				entity.remove(RemovalReason.KILLED);
		}

		// remove nearby dangerous blocks
		for (int x = -SEARCH_HORIZ; x <= SEARCH_HORIZ; x++) {
			for (int y = -SEARCH_VERT; y <= SEARCH_VERT; y++) {
				for (int z = -SEARCH_HORIZ; z <= SEARCH_HORIZ; z++) {
					ServerWorld level = player.getWorld();
					BlockPos pos = BlockPos.ofFloored(player.getPos().add(x, y, z));
					BlockState block = level.getBlockState(pos);
					if (dangerousBlocks.contains(block.getBlock()))
						level.setBlockState(pos, Blocks.AIR.getDefaultState());
				}
			}
		}

		// reset spawn point
//		player.setRespawnPosition(Level.OVERWORLD, null, 0, false, false);

		// alert player
		player.sendMessage(ALERT);
	}

	@Listener
	public void onDeathEvent(Death death) {
		if (death.entity() instanceof ServerPlayerEntity player)
			onDeath(player);
	}
}
