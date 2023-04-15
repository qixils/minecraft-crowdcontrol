package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.common.command.impl.HealthModifierCommand;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSources;
import org.spongepowered.api.event.entity.DamageEntityEvent;

import java.util.Optional;

public final class HealthModifierManager {
	@Listener
	public void onDamage(DamageEntityEvent event) {
		if (!(event.entity() instanceof Player))
			return;
		Player player = (Player) event.entity();
		HealthModifierCommand.Modifier modifier = HealthModifierCommand.ACTIVE_MODIFIERS.get(player.uniqueId());
		if (modifier == null)
			return;
		switch (modifier) {
			case OHKO:
				Optional<DamageSource> source = event.cause().first(DamageSource.class);
				if (source.isPresent() && source.get().equals(DamageSources.VOID) && event.originalDamage() >= Float.MAX_VALUE)
					return; // we've caught ourselves! ignore this damage
				player.damage(Float.MAX_VALUE, DamageSources.VOID);
			case INVINCIBLE:
				event.setCancelled(true);
		}
	}
}
