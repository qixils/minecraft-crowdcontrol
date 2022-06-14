package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.common.EventListener;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge7.TimedCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.time.Duration;
import java.util.List;

@Getter
@EventListener
public class FlightCommand extends TimedCommand {
	private final String effectName = "flight";
	private final String displayName = "Enable Flight";
	private final Duration duration = Duration.ofSeconds(15);

	public FlightCommand(@NotNull SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull Player> ignored, @NotNull Request request) {
		new TimedEffect.Builder()
				.request(request)
				.effectGroup("gamemode")
				.duration(duration)
				.startCallback($ -> {
					List<Player> players = plugin.getPlayers(request);
					Response.Builder response = request.buildResponse()
							.type(ResultType.RETRY)
							.message("Target is already flying or able to fly");
					for (Player player : players) {
						GameMode gameMode = player.gameMode().get();
						if (gameMode.equals(GameModes.CREATIVE))
							continue;
						if (gameMode.equals(GameModes.SPECTATOR))
							continue;
						if (player.get(Keys.CAN_FLY).orElse(false))
							continue;
						if (player.get(Keys.IS_FLYING).orElse(false))
							continue;
						response.type(ResultType.SUCCESS).message("SUCCESS");
						sync(() -> {
							player.offer(Keys.CAN_FLY, true);
							player.offer(Keys.IS_FLYING, true);
						});
					}
					if (response.type() == ResultType.SUCCESS)
						playerAnnounce(players, request);
					return response;
				})
				.completionCallback($ -> {
					List<Player> players = plugin.getPlayers(request);
					sync(() -> players.forEach(player -> {
						player.offer(Keys.CAN_FLY, false);
						player.offer(Keys.IS_FLYING, false);
					}));
				})
				.build().queue();
	}

	@Listener
	public void onJoin(ClientConnectionEvent.Join event) {
		Player player = event.getTargetEntity();
		GameMode gameMode = player.gameMode().get();
		// this is a work of art
		if (gameMode.equals(GameModes.CREATIVE))
			return;
		if (gameMode.equals(GameModes.SPECTATOR))
			return;
		if (!player.get(Keys.CAN_FLY).orElse(false)
				&& !player.get(Keys.IS_FLYING).orElse(false))
			return;
		player.offer(Keys.IS_FLYING, false);
		player.offer(Keys.CAN_FLY, false);
	}
}
