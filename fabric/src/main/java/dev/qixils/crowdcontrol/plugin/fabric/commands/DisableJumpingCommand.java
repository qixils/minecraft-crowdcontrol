package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.common.EventListener;
import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.TimedVoidCommand;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.Components;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Getter
@EventListener
public class DisableJumpingCommand extends TimedVoidCommand {
	private final String effectName = "disable_jumping";

	public DisableJumpingCommand(FabricCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public @NotNull Duration getDefaultDuration() {
		return CommandConstants.DISABLE_JUMPING_DURATION;
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull ServerPlayer> ignored, @NotNull Request request) {
		AtomicReference<List<ServerPlayer>> players = new AtomicReference<>();
		new TimedEffect.Builder().request(request)
				.duration(CommandConstants.DISABLE_JUMPING_DURATION)
				.startCallback($ -> {
					players.set(plugin.getPlayers(request));
					if (players.get().isEmpty())
						return request.buildResponse()
								.type(ResultType.FAILURE)
								.message("No players online");

					for (Player player : players.get())
						Components.JUMP_STATUS.get(player).setProhibited(true);
					playerAnnounce(players.get(), request);

					return null; // success
				})
				.completionCallback($ -> {
					for (Player player : players.get())
						Components.JUMP_STATUS.get(player).setProhibited(false);
				})
				.build().queue();
	}

	// jump cancelling is handled in PlayerMixin
	// TODO: is resetting handled successfully for offline players?
}
