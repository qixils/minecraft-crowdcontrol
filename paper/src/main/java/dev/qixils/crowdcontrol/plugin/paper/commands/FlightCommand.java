package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.paper.PaperCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.CCTimedEffect;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.CCTimedEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;

@Getter
public class FlightCommand extends PaperCommand implements Listener, CCTimedEffect {
	private final String effectName = "flight";
	private final Duration defaultDuration = Duration.ofSeconds(15);
	private final Map<UUID, List<UUID>> uuids = new HashMap<>();

	public FlightCommand(@NotNull PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	private void setFlying(Player player, boolean status) {
		player.getScheduler().run(plugin.getPaperPlugin(), $$ -> {
			player.setAllowFlight(status);
			player.setFlying(status);
			// TODO: velocity
		}, null);
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull Player>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			List<Player> players = playerSupplier.get();
			boolean success = false;
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
				success = true;
				setFlying(player, true);
			}
			if (!success)
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Target is already flying or able to fly");
			uuids.put(request.getRequestId(), players.stream().map(Player::getUniqueId).toList());
			return new CCTimedEffectResponse(request.getRequestId(), ResponseStatus.TIMED_BEGIN, request.getEffect().getDuration() * 1000L);
		}));
	}

	@Override
	public void onPause(@NotNull PublicEffectPayload request, @NotNull CCPlayer source) {
		List<UUID> uuidList = uuids.get(request.getRequestId());
		if (uuidList == null) return;
		uuidList.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(player -> setFlying(player, false));
	}

	@Override
	public void onResume(@NotNull PublicEffectPayload request, @NotNull CCPlayer source) {
		List<UUID> uuidList = uuids.get(request.getRequestId());
		if (uuidList == null) return;
		uuidList.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(player -> setFlying(player, true));
	}

	@Override
	public void onEnd(@NotNull PublicEffectPayload request, @NotNull CCPlayer source) {
		List<UUID> uuidList = uuids.remove(request.getRequestId());
		if (uuidList == null) return;
		uuidList.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(player -> setFlying(player, false));
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
		setFlying(player, false);
	}
}
