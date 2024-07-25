package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.TimedVoidCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;

@Getter
public class FlightCommand extends TimedVoidCommand implements Listener {
	private final String effectName = "flight";
	private final Duration defaultDuration = Duration.ofSeconds(15);

	public FlightCommand(@NotNull PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull Player> ignored, @NotNull Request request) {
		new TimedEffect.Builder()
				.request(request)
				.effectGroup("gamemode")
				.duration(getDuration(request))
				.startCallback($ -> {
					List<Player> players = plugin.getPlayers(request);
					Response.Builder response = request.buildResponse()
							.type(ResultType.RETRY)
							.message("Target is already flying or able to fly");
					for (Player player : players) {
						GameMode gameMode = player.getGameMode();
						if (gameMode == GameMode.CREATIVE)
							continue;
						if (gameMode == GameMode.SPECTATOR)
							continue;
						if (player.getAllowFlight())
							continue;
						if (player.isFlying())
							continue;
						response.type(ResultType.SUCCESS).message("SUCCESS");
						player.getScheduler().run(plugin, $$ -> {
							player.setAllowFlight(true);
							player.setFlying(true);
						}, null);
					}
					if (response.type() == ResultType.SUCCESS)
						announce(players, request);
					return response;
				})
				.completionCallback($ -> {
					List<Player> players = plugin.getPlayers(request);
					players.forEach(player -> player.getScheduler().run(plugin, $$ -> {
						player.setFlying(false);
						player.setAllowFlight(false);
					}, null));
				})
				.build().queue();
	}

	// clear flight on login if they disconnected mid-effect
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		GameMode gamemode = player.getGameMode();
		if (gamemode.equals(GameMode.CREATIVE))
			return;
		if (gamemode.equals(GameMode.SPECTATOR))
			return;
		if (!player.isFlying() && !player.getAllowFlight())
			return;
		player.setFlying(false);
		player.setAllowFlight(false);
	}
}
