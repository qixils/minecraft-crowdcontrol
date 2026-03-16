package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.paper.PaperCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.CCTimedEffect;
import live.crowdcontrol.cc4j.websocket.data.CCTimedEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;

import static dev.qixils.crowdcontrol.plugin.paper.utils.PaperUtil.toPlayers;

@Getter
public class GameModeCommand extends PaperCommand implements CCTimedEffect {
	private final Map<UUID, List<UUID>> activeRequests = new HashMap<>();
	private final Duration defaultDuration;
	private final GameMode gamemode;
	private final Component displayName;
	private final String effectName;
	private final NamespacedKey gamemodeKey;

	private final String effectGroup = "gamemode";
	private final List<String> effectGroups = Collections.singletonList(effectGroup);

	public GameModeCommand(PaperCrowdControlPlugin plugin, GameMode gamemode, long seconds) {
		super(plugin);
		this.gamemodeKey = getGamemodeKey(plugin.getPaperPlugin());
		this.defaultDuration = Duration.ofSeconds(seconds);
		this.gamemode = gamemode;
		this.displayName = Component.translatable(gamemode);
		this.effectName = gamemode.name().toLowerCase() + "_mode";
	}

	private static NamespacedKey getGamemodeKey(Plugin plugin) {
		return new NamespacedKey(plugin, "is_gamemode_active");
	}

	private static boolean isEffectActive(NamespacedKey key, Entity player) {
		return player.getPersistentDataContainer().getOrDefault(key, PaperCrowdControlPlugin.BOOLEAN_TYPE, false);
	}

	public static boolean isEffectActive(@Nullable Plugin plugin, Entity player) {
		if (plugin == null) return false;
		return isEffectActive(getGamemodeKey(plugin), player);
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull Player>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			List<Player> players = playerSupplier.get();
			activeRequests.put(request.getRequestId(), players.stream().map(Player::getUniqueId).toList());
			setGameMode(players, gamemode, true);
			return new CCTimedEffectResponse(request.getRequestId(), ResponseStatus.TIMED_BEGIN, request.getEffect().getDurationMillis());
		}));
	}

	@Override
	public void onEnd(@NotNull PublicEffectPayload request, @NotNull CCPlayer source) {
		List<Player> players = toPlayers(activeRequests.remove(request.getRequestId()));
		setGameMode(players, GameMode.SURVIVAL, false);
	}

	private void setGameMode(@NotNull List<@NotNull Player> players,
							 @NotNull GameMode gamemode,
							 boolean enabling) {
		players.forEach(player -> player.getScheduler().run(plugin.getPaperPlugin(), $ -> {
			player.setGameMode(gamemode);
			player.getPersistentDataContainer().set(gamemodeKey, PaperCrowdControlPlugin.BOOLEAN_TYPE, enabling);
		}, null));
	}

	public static final class Manager implements Listener {
		private final NamespacedKey key;

		public Manager(Plugin plugin) {
			key = getGamemodeKey(plugin);
		}

		@EventHandler
		public void onJoin(PlayerJoinEvent event) {
			Player player = event.getPlayer();
			if (!isEffectActive(key, player)) return;
			player.getPersistentDataContainer().set(key, PaperCrowdControlPlugin.BOOLEAN_TYPE, false);
			player.setGameMode(GameMode.SURVIVAL);
		}
	}
}
