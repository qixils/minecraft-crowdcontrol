package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.TimedCommand;
import dev.qixils.crowdcontrol.socket.Request;
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
				.legacyStartCallback($ -> {
					List<Player> players = plugin.getPlayers(request);
					playerAnnounce(players, request);
					sync(() -> {
						for (Player player : players) {
							player.offer(Keys.CAN_FLY, true);
							player.offer(Keys.IS_FLYING, true);
						}
					});
				})
				.completionCallback($ -> {
					List<Player> players = plugin.getPlayers(request);
					sync(() -> {
						for (Player player : players) {
							player.offer(Keys.CAN_FLY, false);
							player.offer(Keys.IS_FLYING, false);
						}
					});
				})
				.build().queue();
	}

	@Override
	public boolean isEventListener() {
		return true;
	}

	@Listener
	public void onJoin(ClientConnectionEvent.Join event) {
		Player player = event.getTargetEntity();
		GameMode gameMode = player.gameMode().get();
		// this is a work of art
		if (
				(
						gameMode.equals(GameModes.SURVIVAL)
								|| gameMode.equals(GameModes.ADVENTURE)
				) && (
						player.get(Keys.CAN_FLY).orElse(false)
								|| player.get(Keys.IS_FLYING).orElse(false)
				)
		) {
			player.offer(Keys.IS_FLYING, false);
			player.offer(Keys.CAN_FLY, false);
		}
	}
}
