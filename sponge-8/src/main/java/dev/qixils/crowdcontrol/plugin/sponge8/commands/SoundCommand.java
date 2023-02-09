package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.common.util.sound.Sounds;
import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.util.Direction;
import org.spongepowered.math.vector.Vector3d;

import java.util.List;
import java.util.Optional;

@Getter
public class SoundCommand extends ImmediateCommand {
	private final String effectName = "sfx";

	public SoundCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		for (ServerPlayer player : players) {
			Vector3d playAt = player.position();
			Optional<Direction> direction = player.get(Keys.DIRECTION);
			if (direction.isPresent()) {
				playAt = playAt.add(direction.get().opposite().asBlockOffset().toDouble());
			}
			player.playSound(Sounds.SPOOKY.get(), playAt);
		}
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
