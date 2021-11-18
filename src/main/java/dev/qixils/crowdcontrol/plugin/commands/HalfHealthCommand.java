package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class HalfHealthCommand extends ImmediateCommand {
	private final String effectName = "half-health";
	private final String displayName = "Half Health";
	public HalfHealthCommand(CrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Bukkit.getScheduler().runTask(plugin, () -> players.forEach(player -> player.setHealth(player.getHealth()/2)));
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
