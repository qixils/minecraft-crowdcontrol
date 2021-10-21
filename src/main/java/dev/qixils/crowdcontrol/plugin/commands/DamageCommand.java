package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Getter
public class DamageCommand extends ImmediateCommand {
	private final String effectName;
	private final String displayName;
	private final double amount;

	public DamageCommand(CrowdControlPlugin plugin, String effectName, String displayName, double amount) {
		super(plugin);
		this.effectName = effectName;
		this.displayName = displayName;
		this.amount = amount;
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull Request request) {
		boolean success = false;
		for (Player player : CrowdControlPlugin.getPlayers()) {
			if (amount < 0) {
				success = true;
				Bukkit.getScheduler().runTask(plugin, () -> player.setHealth(Math.max(0, Math.min(player.getMaxHealth(), player.getHealth() - amount))));
			} else if (amount >= Short.MAX_VALUE) {
				success = true;
				Bukkit.getScheduler().runTask(plugin, () -> player.setHealth(0));
			} else {
				double oldHealth = player.getHealth();
				double newHealth = Math.min(1, oldHealth - amount);
				double appliedDamage = oldHealth - newHealth;
				if (appliedDamage > 0) {
					success = true;
					Bukkit.getScheduler().runTask(plugin, () -> player.damage(appliedDamage));
				}
			}
		}

		if (success)
			return Response.builder().type(Response.ResultType.SUCCESS);
		else
			return Response.builder().type(Response.ResultType.FAILURE).message("Players would have been killed by this command");
	}
}
