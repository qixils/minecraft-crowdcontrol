package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.common.util.TextUtil;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.TimedCommand;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Getter
public class GamemodeCommand extends TimedCommand {
	private final Duration duration;
	private final GameMode gamemode;
	private final String displayName;
	private final String effectName;
	private final NamespacedKey gamemodeKey;

	public GamemodeCommand(PaperCrowdControlPlugin plugin, GameMode gamemode, long seconds) {
		super(plugin);
		this.gamemodeKey = getGamemodeKey(plugin);
		this.duration = Duration.ofSeconds(seconds);
		this.gamemode = gamemode;
		this.displayName = TextUtil.titleCase(gamemode) + " Mode";
		this.effectName = gamemode.name().toLowerCase(Locale.ENGLISH) + "_mode";
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
	public void voidExecute(@NotNull List<@NotNull Player> ignored, @NotNull Request request) {
		List<Player> players = new ArrayList<>();

		new TimedEffect.Builder()
				.request(request)
				.effectGroup("gamemode")
				.duration(duration)
				.startCallback($ -> {
					List<Player> curPlayers = plugin.getPlayers(request);
					players.addAll(curPlayers);
					setGameMode(request, curPlayers, gamemode);
					announce(players, request);
					return null;
				})
				.completionCallback($ -> setGameMode(null, players, GameMode.SURVIVAL))
				.build().queue();
	}

	private void setGameMode(@Nullable Request request,
							 @NotNull List<@NotNull Player> players,
							 @NotNull GameMode gamemode) {
		if (players.isEmpty())
			return;
		sync(() -> players.forEach(player -> {
			player.setGameMode(gamemode);
			player.getPersistentDataContainer().set(gamemodeKey, PaperCrowdControlPlugin.BOOLEAN_TYPE, request != null);
		}));
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
