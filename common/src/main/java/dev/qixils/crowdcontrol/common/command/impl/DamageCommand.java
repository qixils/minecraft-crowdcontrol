package dev.qixils.crowdcontrol.common.command.impl;

import dev.qixils.crowdcontrol.common.Plugin;
import dev.qixils.crowdcontrol.common.command.ImmediateCommand;
import dev.qixils.crowdcontrol.common.mc.CCPlayer;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

@Getter
public class DamageCommand<P> implements ImmediateCommand<P> {
	private final String effectName;
	private final Component displayName;
	private final double amount;
	private final Plugin<P, ?> plugin;

	public DamageCommand(Plugin<P, ?> plugin, String effectName, double amount) {
		this.plugin = plugin;
		this.effectName = effectName;
		this.displayName = getDefaultDisplayName();
		this.amount = amount;
	}

	public DamageCommand(Plugin<P, ?> plugin, double amount) {
		this.plugin = plugin;

		this.amount = amount;
		int hearts = (int) amount / 2;

		String type = amount > 0 ? "damage" : "heal";
		String key = "cc.effect.generic_" + type + ".name";
		this.effectName = type.toLowerCase(Locale.US) + "_" + hearts;
		this.displayName = Component.translatable(key, Component.text(Math.abs(hearts)));
	}

	@Override
	public @NotNull Component getDisplayName() {
		return displayName;
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull P> players, @NotNull Request request) {
		boolean success = false;

		for (P rawPlayer : players) {
			CCPlayer player = plugin.getPlayer(rawPlayer);
			double oldHealth = player.health();
			double maxHealth = player.maxHealth();
			if (amount < 0) {
				double newHealth = Math.max(0, Math.min(maxHealth, oldHealth - amount));
				if (newHealth != oldHealth) {
					success = true;
					sync(() -> player.health(newHealth));
				}
			} else if (amount >= maxHealth) {
				success = true;
				sync(player::kill);
			} else {
				double newHealth = Math.max(1, oldHealth - amount);
				double appliedDamage = oldHealth - newHealth;
				if (appliedDamage > 0) {
					success = true;
					sync(() -> player.damage(appliedDamage));
				}
			}
		}

		if (success)
			return request.buildResponse()
					.type(Response.ResultType.SUCCESS);
		else
			return request.buildResponse()
					.type(Response.ResultType.RETRY)
					.message("Players would have been killed by this command");
	}
}

