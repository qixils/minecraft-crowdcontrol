package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.storage.ServerWorldProperties;

import java.util.List;

@Getter
public class TimeCommand extends ImmediateCommand {
	private final String effectName = "zip";
	private final String displayName = "Zip Time";

	public TimeCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		if (!isGlobalCommandUsable(players, request))
			return request.buildResponse()
					.type(ResultType.UNAVAILABLE)
					.message("Global command cannot be used on this streamer");

		sync(() -> plugin.getGame().server().worldManager().worlds().forEach(world -> {
			ServerWorldProperties properties = world.properties();
			properties.setDayTime(properties.dayTime().add(0, 12, 0));
		}));
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
