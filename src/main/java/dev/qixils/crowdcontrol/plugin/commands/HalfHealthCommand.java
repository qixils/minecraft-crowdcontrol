package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.Command;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

@Getter
public class HalfHealthCommand extends Command {
	private final String effectName = "half-health";
	private final String displayName = "Half Health";
	public HalfHealthCommand(CrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Result execute(@NotNull Request request) {
		Bukkit.getScheduler().runTask(plugin, () -> CrowdControlPlugin.getPlayers().forEach(player -> player.setHealth(player.getHealth()/2)));
		return Response.Result.SUCCESS;
	}
}
