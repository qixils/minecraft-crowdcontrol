package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.Global;
import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
@Global
public class TimeCommand extends ImmediateCommand {
	private final String effectName = "zip";
	private final String displayName = "Zip Time";

	public TimeCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players,
														@NotNull Request request) {
		sync(() -> Bukkit.getWorlds().forEach(world ->
				world.setFullTime(world.getFullTime() + CommandConstants.ZIP_TIME_TICKS)));
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
