package dev.qixils.crowdcontrol.plugin.sponge7;

import com.flowpowered.math.vector.Vector3d;
import dev.qixils.crowdcontrol.common.SoftLockObserver;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Hostile;
import org.spongepowered.api.entity.living.complex.EnderDragonPart;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DestructEntityEvent.Death;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class SoftLockResolver extends SoftLockObserver<Player> {
	/**
	 * Initializes the observer.
	 *
	 * @param plugin The plugin instance which provides player UUIDs.
	 */
	protected SoftLockResolver(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public void onSoftLock(@NotNull Player player) {
		World world = player.getWorld();
		Vector3d pos = player.getPosition();
		// kill nearby monsters
		for (Entity entity : world.getNearbyEntities(pos, SEARCH_HORIZ)) {
			if (entity instanceof Hostile)
				entity.remove();
			else if (entity instanceof EnderDragonPart) {
				((EnderDragonPart) entity).getParent().remove();
				entity.remove();
			}
		}
		// remove nearby dangerous blocks
		for (int x = -SEARCH_HORIZ; x <= SEARCH_HORIZ; x++) {
			for (int y = -SEARCH_VERT; y <= SEARCH_VERT; y++) {
				for (int z = -SEARCH_HORIZ; z <= SEARCH_HORIZ; z++) {
					Location<World> loc = world.getLocation(pos.add(x, y, z));
					BlockType type = loc.getBlockType();
					if (type.equals(BlockTypes.LAVA) || type.equals(BlockTypes.FIRE) || type.equals(BlockTypes.FLOWING_LAVA)) // API8: wither rose
						loc.setBlockType(BlockTypes.AIR);
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
		plugin.playerMapper().asAudience(player).sendMessage(ALERT);
	}

	@Listener
	public void onDeathEvent(Death event) {
		if (event.getTargetEntity() instanceof Player)
			onDeath((Player) event.getTargetEntity());
	}
}
