package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3d;

import java.util.List;

@Getter
public class UniteCommand extends ImmediateCommand {
	private final String effectName = "unite";

	public UniteCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		if (players.size() < 2)
			return request.buildResponse().type(Response.ResultType.FAILURE).message("Not enough participating players online");

		// the player list passed into this function is already a unique instance and already shuffled
		// so we can just pop the first player off the list and teleport everyone else to them
		Player target = players.remove(0);
		ServerLocation destination = target.serverLocation();
		Vector3d rotation = target.rotation();
		sync(() -> players.forEach(player -> player.setLocationAndRotation(destination, rotation)));
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}

	@Override
	public TriState isSelectable() {
		return plugin.getPlayerManager().getAllPlayers().size() <= 1 ? TriState.FALSE : TriState.TRUE;
	}
}
