package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class RespawnCommand extends ImmediateCommand {
	private final String effectName = "respawn";
	private final String displayName = "Respawn Players";

	public RespawnCommand(FabricCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		sync(() -> players.forEach(Player::respawn)); // TODO: this doesn't work; it's for LAN only, not multiplayer
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
