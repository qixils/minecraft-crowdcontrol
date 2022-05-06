package dev.qixils.crowdcontrol.plugin.mojmap.commands;

import dev.qixils.crowdcontrol.plugin.mojmap.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.mojmap.MojmapPlugin;
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

	public RespawnCommand(MojmapPlugin<?> plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		sync(() -> players.forEach(Player::respawn)); // TODO test this
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
