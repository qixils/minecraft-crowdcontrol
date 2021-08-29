package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.Command;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

@Getter
public class ResetExpProgressCommand extends Command {
	private final String effectName = "reset_exp_progress";
	private final String displayName = "Reset Experience Progress";

	public ResetExpProgressCommand(CrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Result execute(@NotNull Request request) {
		Bukkit.getScheduler().runTask(plugin, () -> CrowdControlPlugin.getPlayers().forEach(player -> player.setExp(0)));
		return Response.Result.SUCCESS;
	}
}
