package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.common.CommandConstants;
import dev.qixils.crowdcontrol.plugin.BukkitCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class TimeCommand extends ImmediateCommand {
    private final String effectName = "zip";
    private final String displayName = "Zip Time";

    public TimeCommand(BukkitCrowdControlPlugin plugin) {
        super(plugin);
    }

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players,
														@NotNull Request request) {
		if (!isGlobalCommandUsable(players, request))
			return request.buildResponse()
					.type(ResultType.UNAVAILABLE)
					.message("Global command cannot be used on this streamer");

		sync(() -> Bukkit.getWorlds().forEach(world ->
				world.setFullTime(world.getFullTime() + CommandConstants.ZIP_TIME_TICKS)));
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
