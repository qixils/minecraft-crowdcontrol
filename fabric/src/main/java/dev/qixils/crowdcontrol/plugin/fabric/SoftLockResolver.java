package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.common.SoftLockObserver;
import dev.qixils.crowdcontrol.plugin.fabric.event.Death;
import dev.qixils.crowdcontrol.plugin.fabric.event.Listener;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.List;

@ParametersAreNonnullByDefault
public final class SoftLockResolver extends SoftLockObserver<ServerPlayer> {

	private final @NotNull Collection<Block> dangerousBlocks = List.of(
			Blocks.LAVA,
			Blocks.FIRE,
			Blocks.WITHER_ROSE
	);
	private final @NotNull TargetingConditions conditions;

	/**
	 * Initializes the observer.
	 *
	 * @param plugin The plugin instance which provides player UUIDs.
	 */
	public SoftLockResolver(FabricCrowdControlPlugin plugin) {
		super(plugin);
		this.conditions = TargetingConditions.forNonCombat()
			.ignoreLineOfSight()
			.ignoreInvisibilityTesting()
			.range(getSearchH());
	}

	@Override
	public void onSoftLock(ServerPlayer player) {
		// kill nearby monsters
		for (Mob entity : player.serverLevel().getNearbyEntities(
				Mob.class,
				conditions,
				player,
				AABB.ofSize(player.position(), getSearchH() *2, getSearchV() *2, getSearchH() *2)
		)) {
			if (entity instanceof Enemy)
				entity.remove(RemovalReason.KILLED);
		}

		// remove nearby dangerous blocks
		for (int x = -getSearchH(); x <= getSearchH(); x++) {
			for (int y = -getSearchV(); y <= getSearchV(); y++) {
				for (int z = -getSearchH(); z <= getSearchH(); z++) {
					ServerLevel level = player.serverLevel();
					BlockPos pos = BlockPos.containing(player.position().add(x, y, z));
					BlockState block = level.getBlockState(pos);
					if (dangerousBlocks.contains(block.getBlock()))
						level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
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
		if (death.entity() instanceof ServerPlayer player)
			onDeath(player);
	}
}
