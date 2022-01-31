package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSources;

import java.util.List;

@Getter
public class DamageCommand extends ImmediateCommand {
	private final String effectName;
	private final String displayName;
	private final double amount;

	public DamageCommand(SpongeCrowdControlPlugin plugin, String effectName, String displayName, double amount) {
		super(plugin);
		this.effectName = effectName;
		this.displayName = displayName;
		this.amount = amount;
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		boolean success = false;
		for (Player player : players) {
			MutableBoundedValue<Double> healthData = player.health();
			if (amount < 0) {
				double oldHealth = healthData.get();
				double newHealth = Math.max(0, Math.min(player.maxHealth().get(), oldHealth - amount));
				if (newHealth != oldHealth) {
					success = true;
					sync(() -> player.offer(healthData.set(newHealth)));
				}
			} else if (amount >= Short.MAX_VALUE) {
				success = true;
				sync(() -> player.offer(healthData.set(0d)));
			} else {
				double oldHealth = healthData.get();
				double newHealth = Math.max(1, oldHealth - amount);
				double appliedDamage = oldHealth - newHealth;
				if (appliedDamage > 0) {
					success = true;
					sync(() -> player.damage(appliedDamage, DamageSources.MAGIC));
				}
			}
		}

		if (success)
			return request.buildResponse()
					.type(Response.ResultType.SUCCESS);
		else
			return request.buildResponse()
					.type(Response.ResultType.FAILURE)
					.message("Players would have been killed by this command");
	}
}
