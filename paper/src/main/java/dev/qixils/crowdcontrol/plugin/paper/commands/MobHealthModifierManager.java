package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.command.impl.MobHealthModifierCommand;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public final class MobHealthModifierManager implements Listener {
	@EventHandler
	public void onDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof LivingEntity target))
			return;
		if (target instanceof ServerPlayer)
			return; // no PvP

		// modify how players interact with this entity
		Entity source = event.getDamageSource().getCausingEntity();
		if (source == null)
			return;
		MobHealthModifierCommand.Modifier modifier = MobHealthModifierCommand.ACTIVE_MODIFIERS.get(source.getUniqueId());
		if (modifier == null)
			return;

		switch (modifier) {
			case OHKO:
				event.setDamage(1000);
				break;
			case INVINCIBLE:
				event.setCancelled(true);
				break;
		}
	}
}
