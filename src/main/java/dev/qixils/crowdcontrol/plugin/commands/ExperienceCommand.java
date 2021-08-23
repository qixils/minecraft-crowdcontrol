package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.Command;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

@Getter
public class ExperienceCommand extends Command {
	private final String effectName;
	private final String displayName;
	private final int amount;

	public ExperienceCommand(CrowdControlPlugin plugin, String effectName, String displayName, int amount) {
		super(plugin);
		this.effectName = effectName;
		this.displayName = displayName;
		this.amount = amount;
	}

	@Override
	public Response.@NotNull Result execute(@NotNull Request request) {
		Bukkit.getScheduler().runTask(plugin, () -> CrowdControlPlugin.getPlayers().forEach(player -> player.setLevel(player.getLevel() + amount)));
		return Response.Result.SUCCESS;
	}
}
