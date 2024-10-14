package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.utils.Location;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class UniteCommand extends ImmediateCommand {
	private final String effectName = "unite";

	public UniteCommand(ModdedCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		if (players.size() < 2)
			return request.buildResponse().type(ResultType.FAILURE).message("Not enough participating players online");

		// the player list passed into this function is already a unique instance and already shuffled
		// so we can just pop the first player off the list and teleport everyone else to them
		ServerPlayer target = players.remove(0);
		Location destination = new Location(target);
		sync(() -> players.forEach(destination::teleportHere));
		return request.buildResponse().type(ResultType.SUCCESS);
	}

	@Override
	public TriState isSelectable() {
		return plugin.getPlayerManager().getAllPlayers().size() <= 1 ? TriState.FALSE : TriState.TRUE;
	}
}
