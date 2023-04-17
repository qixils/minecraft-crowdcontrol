package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.command.impl.HealthModifierCommand;
import dev.qixils.crowdcontrol.plugin.fabric.event.Damage;
import dev.qixils.crowdcontrol.plugin.fabric.event.Listener;
import net.minecraft.server.network.ServerPlayerEntity;

public final class HealthModifierManager {
	@Listener
	public void onDamage(Damage event) {
		if (!(event.entity() instanceof ServerPlayerEntity player))
			return;
		HealthModifierCommand.Modifier modifier = HealthModifierCommand.ACTIVE_MODIFIERS.get(player.getUuid());
		if (modifier == null)
			return;
		switch (modifier) {
			case OHKO:
				if (event.source().equals(player.getDamageSources().outOfWorld()) && event.amount() >= Float.MAX_VALUE)
					return; // we've caught ourselves! ignore this damage
				player.kill();
			case INVINCIBLE:
				event.cancel();
		}
	}
}
