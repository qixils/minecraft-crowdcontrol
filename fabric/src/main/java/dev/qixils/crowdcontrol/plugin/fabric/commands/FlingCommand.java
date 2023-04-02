package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class FlingCommand extends ImmediateCommand {
	private final @NotNull String effectName = "fling";

	public FlingCommand(@NotNull FabricCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	private static Vec3d randomVector() {
		double[] vector = CommandConstants.randomFlingVector();
		return new Vec3d(vector[0], vector[1], vector[2]);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayerEntity> players, @NotNull Request request) {
		for (ServerPlayerEntity player : players) sync(() -> {
			player.setVelocity(randomVector());
			player.velocityModified = true;
		});
		return request.buildResponse().type(ResultType.SUCCESS);
	}
}
