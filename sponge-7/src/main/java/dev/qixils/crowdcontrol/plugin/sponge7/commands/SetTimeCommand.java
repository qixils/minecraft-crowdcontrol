package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.common.Global;
import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.List;

@Getter
@Global
public class SetTimeCommand extends ImmediateCommand {
	private final @NotNull String effectName;
	private final long time;

	public SetTimeCommand(SpongeCrowdControlPlugin plugin, String effectName, long time) {
		super(plugin);
		this.effectName = effectName;
		this.time = time;
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		for (World world : plugin.getGame().getServer().getWorlds()) {
			WorldProperties properties = world.getProperties();
			long setTime = properties.getWorldTime();
			while ((setTime % 24000) != time)
				// I could be awesome and use the prefix add operator in the while condition and use
				// an empty body but then IntelliJ would yell at me and I don't like being yelled at
				// :(
				setTime++;
			final long finalSetTime = setTime;
			sync(() -> world.getProperties().setWorldTime(finalSetTime));
		}
		return request.buildResponse().type(ResultType.SUCCESS);
	}
}
