package dev.qixils.crowdcontrol.plugin.paper;

import dev.qixils.crowdcontrol.common.SoftLockObserver;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Set;

public class SoftLockResolver extends SoftLockObserver<Player> implements Listener {
	private static final Set<Material> DANGEROUS_BLOCKS = EnumSet.of(
			Material.LAVA,
			Material.WITHER_ROSE,
			Material.FIRE
	);

	/**
	 * Initializes the observer.
	 *
	 * @param plugin The plugin instance which provides player UUIDs.
	 */
	protected SoftLockResolver(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public void onSoftLock(@NotNull Player player) {
		Location location = player.getLocation();
		// kill nearby monsters
		for (Entity entity : location.getNearbyEntities(getSearchH(), getSearchV(), getSearchH())) {
			if (entity instanceof Monster || entity instanceof EnderDragon)
				entity.remove();
			else if (entity instanceof EnderDragonPart enderDragonPart) {
				enderDragonPart.getParent().remove();
				enderDragonPart.remove();
			}
		}
		// remove nearby dangerous blocks
		for (int x = -getSearchH(); x <= getSearchH(); x++) {
			for (int y = -getSearchV(); y <= getSearchV(); y++) {
				for (int z = -getSearchH(); z <= getSearchH(); z++) {
					Location blockLocation = location.clone().add(x, y, z);
					Block block = blockLocation.getBlock();
					Material material = block.getType();
					if (DANGEROUS_BLOCKS.contains(material))
						block.setType(Material.AIR);
				}
			}
		}
		// reset spawn point
//		player.setBedSpawnLocation(null);
		// inform player
		player.sendMessage(ALERT);
	}

	@EventHandler
	public void onDeathEvent(PlayerDeathEvent event) {
		// ignore PVP deaths
		if (event.getEntity().getKiller() != null)
			return;

		onDeath(event.getEntity());
	}
}
