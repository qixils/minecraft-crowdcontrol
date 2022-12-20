package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.Global;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.DAY;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.NIGHT;

@Getter
@Global
public class SetTimeCommand extends ImmediateCommand {
	private final @NotNull String effectName;
	private final long time;

	public SetTimeCommand(FabricCrowdControlPlugin plugin, @NotNull String effectName, long time) {
		super(plugin);
		this.effectName = effectName;
		this.time = time;
	}

	@SuppressWarnings("UnnecessaryContinue")
	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		for (ServerLevel level : plugin.server().getAllLevels()) {
			long setTime = level.getDayTime();
			while ((setTime++ % 24000) != time)
				continue;
			final long finalSetTime = setTime;
			sync(() -> level.setDayTime(finalSetTime));
		}
		return request.buildResponse().type(ResultType.SUCCESS);
	}

	@NotNull
	public static SetTimeCommand day(FabricCrowdControlPlugin plugin) {
		return new SetTimeCommand(plugin, "time_day", DAY);
	}

	@NotNull
	public static SetTimeCommand night(FabricCrowdControlPlugin plugin) {
		return new SetTimeCommand(plugin, "time_night", NIGHT);
	}
}
