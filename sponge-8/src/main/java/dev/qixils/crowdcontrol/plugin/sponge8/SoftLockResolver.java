package dev.qixils.crowdcontrol.plugin.sponge8;

import dev.qixils.crowdcontrol.common.SoftLockObserver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Hostile;
import org.spongepowered.api.entity.living.monster.boss.dragon.EnderDragonPart;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DestructEntityEvent.Death;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Collection;

@ParametersAreNonnullByDefault
public class SoftLockResolver extends SoftLockObserver<ServerPlayer> {
	private @Nullable Collection<BlockType> dangerousBlocks;

	/**
	 * Initializes the observer.
	 *
	 * @param plugin The plugin instance which provides player UUIDs.
	 */
	protected SoftLockResolver(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	public @NotNull Collection<BlockType> getDangerousBlocks() {
		if (dangerousBlocks != null) return dangerousBlocks;
		return dangerousBlocks = Arrays.asList(
				BlockTypes.LAVA.get(),
				BlockTypes.FIRE.get(),
				BlockTypes.WITHER_ROSE.get()
		);
	}

	@Override
	public void onSoftLock(ServerPlayer player) {
		ServerWorld world = player.world();
		Vector3d pos = player.position();
		// kill nearby monsters
		for (Entity entity : world.nearbyEntities(pos, getSearchH())) {
			if (entity instanceof Hostile)
				entity.remove();
			else if (entity instanceof EnderDragonPart) {
				((EnderDragonPart) entity).parent().remove();
				entity.remove();
			}
		}
		// remove nearby dangerous blocks
		for (int x = -getSearchH(); x <= getSearchH(); x++) {
			for (int y = -getSearchV(); y <= getSearchV(); y++) {
				for (int z = -getSearchH(); z <= getSearchH(); z++) {
					ServerLocation loc = world.location(pos.add(x, y, z));
					BlockType type = loc.blockType();
					if (getDangerousBlocks().contains(type))
						loc.setBlockType(BlockTypes.AIR.get());
				}
			}
		}
		// reset spawn point
//		player.transform(Keys.RESPAWN_LOCATIONS, map -> {
//			if (map == null) return null;
//			map.remove(world.getUniqueId());
//			return map;
//		});
		// alert player
		player.sendMessage(ALERT);
	}

	@Listener
	public void onDeathEvent(Death event) {
		if (event.entity() instanceof ServerPlayer)
			onDeath((ServerPlayer) event.entity());
	}
}
