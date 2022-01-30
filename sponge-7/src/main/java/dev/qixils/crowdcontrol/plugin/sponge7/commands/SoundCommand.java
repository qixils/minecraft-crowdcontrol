package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import com.flowpowered.math.vector.Vector3d;
import dev.qixils.crowdcontrol.common.util.sound.Sounds;
import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.Direction;

import java.util.List;
import java.util.Optional;

@Getter
public class SoundCommand extends ImmediateCommand {
	private final String effectName = "sfx";
	private final String displayName = "Spooky Sound Effect";

	public SoundCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		for (Player player : players) {
			Vector3d playAt = player.getPosition();
			Optional<Direction> direction = player.get(Keys.DIRECTION);
			if (direction.isPresent()) {
				playAt = playAt.add(direction.get().getOpposite().asBlockOffset().toDouble());
			}
			plugin.asAudience(player).playSound(
					Sounds.SPOOKY.get(),
					playAt.getX(),
					playAt.getY(),
					playAt.getZ()
			);
		}
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
