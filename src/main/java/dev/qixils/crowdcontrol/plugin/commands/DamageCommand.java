package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.Command;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

@Getter
public class DamageCommand extends Command {
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
	public Response.@NotNull Result execute(@NotNull Request request) {
		Bukkit.getScheduler().runTask(plugin, () -> CrowdControlPlugin.getPlayers().forEach(player -> {
			if (amount < 0)
				player.setHealth(Math.max(0, Math.min(player.getMaxHealth(), player.getHealth() - amount)));
			else {
				if (amount >= Short.MAX_VALUE)
					player.setHealth(0);
				else
					player.damage(amount);
			}
		}));
		return Response.Result.SUCCESS;
	}
}
