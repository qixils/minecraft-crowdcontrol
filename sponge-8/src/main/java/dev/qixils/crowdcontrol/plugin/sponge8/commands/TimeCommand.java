package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.common.Global;
import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.storage.ServerWorldProperties;

import java.util.List;

@Getter
@Global
public class TimeCommand extends ImmediateCommand {
	private final String effectName = "zip";

	public TimeCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		sync(() -> plugin.getGame().server().worldManager().worlds().forEach(world -> {
			ServerWorldProperties properties = world.properties();
			properties.setDayTime(properties.dayTime().add(0, 12, 0));
		}));
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
