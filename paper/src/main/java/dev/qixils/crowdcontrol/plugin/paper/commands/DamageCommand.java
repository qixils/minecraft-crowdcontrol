package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class DamageCommand extends ImmediateCommand {
	private final String effectName;
	private final String displayName;
	private final double amount;

	public DamageCommand(PaperCrowdControlPlugin plugin, String effectName, String displayName, double amount) {
		super(plugin);
		this.effectName = effectName;
		this.displayName = displayName;
		this.amount = amount;
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		boolean success = false;
		for (Player player : players) {
			if (amount < 0) {
				double oldHealth = player.getHealth();
				double newHealth = Math.max(0, Math.min(player.getMaxHealth(), oldHealth - amount));
				if (newHealth != oldHealth) {
					success = true;
					sync(() -> player.setHealth(newHealth));
				}
			} else if (amount >= Short.MAX_VALUE) {
				success = true;
				sync(() -> player.setHealth(0));
			} else {
				double oldHealth = player.getHealth();
				double newHealth = Math.max(1, oldHealth - amount);
				double appliedDamage = oldHealth - newHealth;
				if (appliedDamage > 0) {
					success = true;
					sync(() -> player.damage(appliedDamage));
				}
			}
		}

		if (success)
			return request.buildResponse().type(Response.ResultType.SUCCESS);
		else
			return request.buildResponse().type(Response.ResultType.FAILURE).message("Players would have been killed by this command");
	}
}
