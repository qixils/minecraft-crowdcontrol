package dev.qixils.crowdcontrol.plugin.mojmap.commands;

import dev.qixils.crowdcontrol.common.CommandConstants;
import dev.qixils.crowdcontrol.common.Global;
import dev.qixils.crowdcontrol.plugin.mojmap.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.mojmap.MojmapPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
@Global
public class TimeCommand extends ImmediateCommand {
	private final String effectName = "zip";
	private final String displayName = "Zip Time";

	public TimeCommand(MojmapPlugin<?> plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players,
														@NotNull Request request) {
		sync(() -> plugin.server().getAllLevels().forEach(world ->
				world.setDayTime(world.getDayTime() + CommandConstants.ZIP_TIME_TICKS)));
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
