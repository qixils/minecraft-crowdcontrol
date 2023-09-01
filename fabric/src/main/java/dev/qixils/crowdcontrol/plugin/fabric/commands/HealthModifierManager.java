package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.command.impl.HealthModifierCommand;
import dev.qixils.crowdcontrol.plugin.fabric.event.Damage;
import dev.qixils.crowdcontrol.plugin.fabric.event.Listener;
import net.minecraft.server.level.ServerPlayer;

public final class HealthModifierManager {
	@Listener
	public void onDamage(Damage event) {
		if (!(event.entity() instanceof ServerPlayer player))
			return;
		HealthModifierCommand.Modifier modifier = HealthModifierCommand.ACTIVE_MODIFIERS.get(player.getUUID());
		if (modifier == null)
			return;
		switch (modifier) {
			case OHKO:
				if (event.source().equals(player.damageSources().outOfWorld()) && event.amount() >= Float.MAX_VALUE)
					return; // we've caught ourselves! ignore this damage
				player.hurt(player.damageSources().outOfWorld(), Float.MAX_VALUE);
			case INVINCIBLE:
				event.cancel();
		}
	}
}
