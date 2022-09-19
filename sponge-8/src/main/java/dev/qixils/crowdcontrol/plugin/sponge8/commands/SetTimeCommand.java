package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.common.Global;
import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Server;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.util.MinecraftDayTime;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.server.storage.ServerWorldProperties;

import java.util.List;

@Getter
@Global
public class SetTimeCommand extends ImmediateCommand {
	private final @NotNull String effectName;
	private final @NotNull Ticks time;

	public SetTimeCommand(SpongeCrowdControlPlugin plugin, String effectName, long time) {
		super(plugin);
		this.effectName = effectName;
		this.time = Ticks.of(time);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Server server = plugin.getGame().server();
		for (ServerWorld world : server.worldManager().worlds()) {
			ServerWorldProperties properties = world.properties();
			sync(() -> properties.setDayTime(MinecraftDayTime.of(server, time)));
		}
		return request.buildResponse().type(ResultType.SUCCESS);
	}
}
