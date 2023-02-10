package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.command.impl.HealthModifierCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public final class HealthModifierManager implements Listener {
	@EventHandler
	public void onDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player player))
			return;
		HealthModifierCommand.Modifier modifier = HealthModifierCommand.ACTIVE_MODIFIERS.get(player.getUniqueId());
		if (modifier == null)
			return;
		switch (modifier) {
			case OHKO:
				player.setHealth(0);
			case INVINCIBLE:
				event.setCancelled(true);
		}
	}
}
