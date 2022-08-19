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

@Getter
@Global
public class SetTimeCommand extends ImmediateCommand {
	private final @NotNull String displayName;
	private final @NotNull String effectName;
	private final long time;

	public SetTimeCommand(FabricCrowdControlPlugin plugin, @NotNull String displayName, @NotNull String effectName, long time) {
		super(plugin);
		this.displayName = displayName;
		this.effectName = effectName;
		this.time = time;
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		for (ServerLevel level : plugin.server().getAllLevels()) {
			// TODO: this resets the day to 0
			sync(() -> level.setDayTime(time));
		}
		return request.buildResponse().type(ResultType.SUCCESS);
	}
}
