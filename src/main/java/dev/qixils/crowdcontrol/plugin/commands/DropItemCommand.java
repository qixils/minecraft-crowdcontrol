package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.Command;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Getter
public class DropItemCommand extends Command {
	public DropItemCommand(CrowdControlPlugin plugin) {
		super(plugin);
	}

	private final String effectName = "drop-item";
	private final String displayName = "Drop Held Item";

	@Override
	public Response.@NotNull Result execute(@NotNull Request request) {
		Response.Result result = new Response.Result(Response.ResultType.FAILURE, "No players were holding items");
		for (Player player : CrowdControlPlugin.getPlayers()) {
			if (player.dropItem(true))
				result = Response.Result.SUCCESS;
		}
		return result;
	}
}
