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
public class ResetExpProgressCommand extends ImmediateCommand {
	private final String effectName = "reset_exp_progress";
	private final String displayName = "Reset Experience Progress";

	public ResetExpProgressCommand(CrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull Request request) {
		Response.Builder result = request.buildResponse().type(Response.ResultType.FAILURE).message("No players have XP");
		for (Player player : CrowdControlPlugin.getPlayers()) {
			if (player.getExp() > 0) {
				result.type(Response.ResultType.SUCCESS).message("SUCCESS");
				Bukkit.getScheduler().runTask(plugin, () -> player.setExp(0));
			}
		}
		return result;
	}
}
