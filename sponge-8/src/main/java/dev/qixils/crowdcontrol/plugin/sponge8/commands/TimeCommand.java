package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.common.CommandConstants;
import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.storage.WorldProperties;

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
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		if (!isGlobalCommandUsable(players, request))
			return request.buildResponse()
					.type(ResultType.UNAVAILABLE)
					.message("Global command cannot be used on this streamer");

		sync(() -> plugin.getGame().getServer().getWorlds().forEach(world -> {
			WorldProperties properties = world.getProperties();
			properties.setWorldTime(properties.getWorldTime() + CommandConstants.ZIP_TIME_TICKS);
		}));
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
