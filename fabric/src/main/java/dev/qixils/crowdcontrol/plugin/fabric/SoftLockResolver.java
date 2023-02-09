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
	private final @NotNull TargetingConditions conditions = TargetingConditions.forNonCombat()
			.ignoreLineOfSight()
			.ignoreInvisibilityTesting()
			.range(SEARCH_HORIZ);

	/**
	 * Initializes the observer.
	 *
	 * @param plugin The plugin instance which provides player UUIDs.
	 */
	public SoftLockResolver(FabricCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public void onSoftLock(ServerPlayer player) {
		// kill nearby monsters
		for (Mob entity : player.getLevel().getNearbyEntities(
				Mob.class,
				conditions,
				player,
				AABB.ofSize(player.position(), SEARCH_HORIZ*2, SEARCH_VERT*2, SEARCH_HORIZ*2)
		)) {
			if (entity instanceof Enemy)
				entity.remove(RemovalReason.KILLED);
		}

		// remove nearby dangerous blocks
		for (int x = -SEARCH_HORIZ; x <= SEARCH_HORIZ; x++) {
			for (int y = -SEARCH_VERT; y <= SEARCH_VERT; y++) {
				for (int z = -SEARCH_HORIZ; z <= SEARCH_HORIZ; z++) {
					ServerLevel level = player.getLevel();
					BlockPos pos = new BlockPos(player.position().add(x, y, z));
					BlockState block = level.getBlockState(pos);
					if (dangerousBlocks.contains(block.getBlock()))
						level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
				}
			}
		}

		// reset spawn point
//		player.setRespawnPosition(Level.OVERWORLD, null, 0, false, false);

		// alert player
		plugin.translator().wrap(player).sendMessage(ALERT);
	}

	@Listener
	public void onDeathEvent(Death death) {
		if (death.entity() instanceof ServerPlayer player)
			onDeath(player);
	}
}
