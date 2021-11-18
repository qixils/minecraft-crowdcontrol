package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.TimedCommand;
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
		new TimedEffect(request, "gamemode", duration,
				$ -> CrowdControlPlugin.getPlayers(request).thenAccept(curPlayers -> curPlayers.forEach(player -> Bukkit.getScheduler().runTask(plugin, () -> {
					player.setAllowFlight(true);
					player.setFlying(true);
				}))), $ -> CrowdControlPlugin.getPlayers(request).thenAccept(curPlayers -> curPlayers.forEach(player -> Bukkit.getScheduler().runTask(plugin, () -> {
					player.setFlying(false);
					player.setAllowFlight(false);
		}))));
	}

	// clear flight on login if they disconnected mid-effect
	@EventHandler
	public void onLogin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		GameMode gamemode = player.getGameMode();
		if ((gamemode == GameMode.SURVIVAL || gamemode == GameMode.ADVENTURE) && (player.getAllowFlight() || player.isFlying())) {
			player.setFlying(false);
			player.setAllowFlight(false);
		}
	}
}