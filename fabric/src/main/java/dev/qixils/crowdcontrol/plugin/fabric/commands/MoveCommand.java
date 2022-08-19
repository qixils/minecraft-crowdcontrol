package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public final class MoveCommand extends ImmediateCommand {
	private final Vec3 vector;
	private final String effectName;
	private final String displayName;

	public MoveCommand(FabricCrowdControlPlugin plugin, Vec3 displacement, String effectName, String displayName) {
		super(plugin);
		vector = displacement;
		this.effectName = effectName;
		this.displayName = "Fling " + displayName;
	}

	public MoveCommand(FabricCrowdControlPlugin plugin, double x, double y, double z, String effectName, String displayName) {
		this(plugin, new Vec3(x, y, z), effectName, displayName);
	}

	public MoveCommand(FabricCrowdControlPlugin plugin, Vec3 displacement, String effectName) {
		this(plugin, displacement, effectName, effectName);
	}

	public MoveCommand(FabricCrowdControlPlugin plugin, double x, double y, double z, String effectName) {
		this(plugin, x, y, z, effectName, effectName);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		if (vector.y() > 0.0 && TimedEffect.isActive("disable_jumping", request.getTargets()))
			// TODO: remove this? or implement something similar to the TODO comment in Sponge8's impl
			return request.buildResponse().type(ResultType.RETRY).message("Effect cannot be used while Disable Jumping is active");

		Response.Builder response = request.buildResponse().type(ResultType.RETRY).message("All players were grounded");
		boolean isDownwards = vector.y() < 0.0;
		for (ServerPlayer player : players) {
			if (isDownwards && player.isOnGround())
				continue;
			response.type(ResultType.SUCCESS).message("SUCCESS");
			player.setDeltaMovement(vector);
			player.hurtMarked = true;
		}
		return response;
	}
}
