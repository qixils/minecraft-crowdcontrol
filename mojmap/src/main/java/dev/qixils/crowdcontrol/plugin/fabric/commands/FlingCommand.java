package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.ExecuteUsing;
import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.qixils.crowdcontrol.TimedEffect.isActive;

@Getter
@ExecuteUsing(ExecuteUsing.Type.SYNC_GLOBAL)
public class FlingCommand extends ImmediateCommand {
	private final @NotNull String effectName = "fling";

	public FlingCommand(@NotNull ModdedCrowdControlPlugin plugin) {
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
		if (isActive("walk", request) || isActive("look", request))
			return request.buildResponse().type(ResultType.RETRY).message("Cannot fling while frozen");

		boolean success = false;
		for (ServerPlayer player : players) {
			if (player.isPassenger()) continue;

			player.setDeltaMovement(randomVector());
			player.hurtMarked = true;
			success = true;
		}

		return success
			? request.buildResponse().type(ResultType.SUCCESS)
			: request.buildResponse().type(ResultType.RETRY).message("Cannot fling while inside vehicle");
	}
}
