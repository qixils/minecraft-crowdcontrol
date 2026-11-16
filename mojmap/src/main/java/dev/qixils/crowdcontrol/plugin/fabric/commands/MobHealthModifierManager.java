package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.command.impl.MobHealthModifierCommand;
import dev.qixils.crowdcontrol.plugin.fabric.event.Damage;
import dev.qixils.crowdcontrol.plugin.fabric.event.Listener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public final class MobHealthModifierManager {
	@Listener
	public void onDamage(Damage event) {
		if (event.entity() instanceof ServerPlayer)
			return; // no PvP

		Level level = event.entity().level();
		if (!(level instanceof ServerLevel serverLevel))
			return;

		// modify how players interact with this entity
		Entity source = event.source().getEntity();
		if (source == null)
			return;
		MobHealthModifierCommand.Modifier modifier = MobHealthModifierCommand.ACTIVE_MODIFIERS.get(source.getUUID());
		if (modifier == null)
			return;

		switch (modifier) {
			case OHKO:
				event.amount(1000);
				break;
			case INVINCIBLE:
				event.cancel();
				break;
		}
	}
}
