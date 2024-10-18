package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import com.flowpowered.math.vector.Vector3d;
import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Response;
import live.crowdcontrol.cc4j.IUserRecord;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;

@Getter
public class UniteCommand extends ImmediateCommand {
	private final String effectName = "unite";

	public UniteCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		if (players.size() < 2)
			return request.buildResponse().type(Response.ResultType.FAILURE).message("Not enough participating players online");

		// the player list passed into this function is already a unique instance and already shuffled
		// so we can just pop the first player off the list and teleport everyone else to them
		Player target = players.remove(0);
		Location<World> destination = target.getLocation();
		Vector3d rotation = target.getRotation();
		sync(() -> players.forEach(player -> player.setLocationAndRotation(destination, rotation)));
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}

	@Override
	public TriState isSelectable(@NotNull IUserRecord user, @NotNull List<Player> potentialPlayers) {
		return plugin.getPlayerManager().getAllPlayers().size() <= 1 ? TriState.FALSE : TriState.TRUE;
	}
}
