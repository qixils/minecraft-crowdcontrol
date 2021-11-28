package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.TimedCommand;
import dev.qixils.crowdcontrol.plugin.utils.PlayerListWrapper;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;

@Getter
public class FlightCommand extends TimedCommand implements Listener {
	private final String effectName = "flight";
	private final String displayName = "Enable Flight";
	private final Duration duration = Duration.ofSeconds(15);

	public FlightCommand(@NotNull CrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	protected void voidExecute(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		PlayerListWrapper startWrapper = new PlayerListWrapper(request, curPlayers -> {
			announce(curPlayers, request);
			curPlayers.forEach(player -> Bukkit.getScheduler().runTask(plugin, () -> {
				player.setAllowFlight(true);
				player.setFlying(true);
			}));
		});

		PlayerListWrapper endWrapper = new PlayerListWrapper(request,
				curPlayers -> curPlayers.forEach(player -> Bukkit.getScheduler().runTask(plugin, () -> {
					player.setFlying(false);
					player.setAllowFlight(false);
				}))
		);

		new TimedEffect(request, "gamemode", duration,
				$ -> plugin.getPlayers(request).whenComplete(startWrapper),
				$ -> plugin.getPlayers(request).whenComplete(endWrapper)
		).queue();
	}

	// clear flight on login if they disconnected mid-effect
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		GameMode gamemode = player.getGameMode();
		if ((gamemode == GameMode.SURVIVAL || gamemode == GameMode.ADVENTURE) && (player.getAllowFlight() || player.isFlying())) {
			player.setFlying(false);
			player.setAllowFlight(false);
		}
	}
}
