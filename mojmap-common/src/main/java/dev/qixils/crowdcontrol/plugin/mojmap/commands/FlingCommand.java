package dev.qixils.crowdcontrol.plugin.mojmap.commands;

import dev.qixils.crowdcontrol.common.CommandConstants;
import dev.qixils.crowdcontrol.plugin.mojmap.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.mojmap.MojmapPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class FlingCommand extends ImmediateCommand {
	private final @NotNull String displayName = "Fling Randomly";
	private final @NotNull String effectName = "fling";

	public FlingCommand(@NotNull MojmapPlugin plugin) {
		super(plugin);
	}

	@NotNull
	private static Vec3 randomVector() {
		double[] vector = CommandConstants.randomFlingVector();
		return new Vec3(vector[0], vector[1], vector[2]);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		for (ServerPlayer player : players)
			sync(() -> player.setDeltaMovement(randomVector()));
		return request.buildResponse().type(ResultType.SUCCESS);
	}
}
