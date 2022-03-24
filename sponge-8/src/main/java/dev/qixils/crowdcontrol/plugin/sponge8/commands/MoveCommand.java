package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.math.vector.Vector3d;

import java.util.List;

@Getter
public final class MoveCommand extends ImmediateCommand {
	private final Vector3d vector;
	private final String effectName;
	private final String displayName;

	public MoveCommand(SpongeCrowdControlPlugin plugin, Vector3d displacement, String effectName, String displayName) {
		super(plugin);
		vector = displacement;
		this.effectName = effectName;
		this.displayName = "Fling " + displayName;
	}

	public MoveCommand(SpongeCrowdControlPlugin plugin, double x, double y, double z, String effectName, String displayName) {
		this(plugin, new Vector3d(x, y, z), effectName, displayName);
	}

	public MoveCommand(SpongeCrowdControlPlugin plugin, Vector3d displacement, String effectName) {
		this(plugin, displacement, effectName, effectName);
	}

	public MoveCommand(SpongeCrowdControlPlugin plugin, double x, double y, double z, String effectName) {
		this(plugin, x, y, z, effectName, effectName);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		if (vector.y() > 0.0 && TimedEffect.isActive("disable_jumping", request.getTargets()))
			// TODO: why not just have a collection of users being affected which Disable Jumping can check against?
			return request.buildResponse().type(ResultType.RETRY).message("Effect cannot be used while Disable Jumping is active");

		Response.Builder response = request.buildResponse().type(ResultType.RETRY).message("All players were grounded");
		boolean isDownwards = vector.y() < 0.0;
		for (ServerPlayer player : players) {
			if (isDownwards && player.onGround().get())
				continue;
			response.type(ResultType.SUCCESS).message("SUCCESS");
			sync(() -> {
				try (StackFrame frame = plugin.getGame().server().causeStackManager().pushCauseFrame()) {
					frame.pushCause(plugin.getPluginContainer());
					player.offer(Keys.VELOCITY, vector);
					frame.popCause();
				}
			});
		}
		return response;
	}
}
