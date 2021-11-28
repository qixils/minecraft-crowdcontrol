package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.TimedCommand;
import dev.qixils.crowdcontrol.plugin.utils.PlayerListWrapper;
import dev.qixils.crowdcontrol.plugin.utils.TextUtil;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Getter
public class GamemodeCommand extends TimedCommand implements Listener {
	private static final PersistentDataType<Byte, Boolean> BOOLEAN = new BooleanDataType();

	private final Duration duration;
	private final GameMode gamemode;
	private final String displayName;
	private final String effectName;
	private final NamespacedKey gamemodeKey;

	public GamemodeCommand(CrowdControlPlugin plugin, GameMode gamemode, long seconds) {
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
		return player.getPersistentDataContainer().getOrDefault(key, BOOLEAN, false);
	}

	public static boolean isEffectActive(Plugin plugin, Entity player) {
		if (plugin == null) return false;
		return isEffectActive(getGamemodeKey(plugin), player);
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull Player> ignored, @NotNull Request request) {
		List<Player> players = new ArrayList<>();

		PlayerListWrapper wrapper = new PlayerListWrapper(request,
				curPlayers -> players.addAll(setGameMode(request, curPlayers, gamemode))
		);

		new TimedEffect(request, "gamemode", duration,
				$ -> plugin.getPlayers(request).whenComplete(wrapper),
				$ -> setGameMode(null, players, GameMode.SURVIVAL)).queue();
	}

	private List<Player> setGameMode(@Nullable Request request,
									 @NotNull List<@NotNull Player> players,
									 @NotNull GameMode gamemode) {
		if (players.isEmpty()) return players;
		if (request != null)
			announce(players, request);
		for (Player player : players) {
			if (player.isValid())
				Bukkit.getScheduler().runTask(plugin, () -> {
					player.setGameMode(gamemode);
					player.getPersistentDataContainer().set(gamemodeKey, BOOLEAN, request != null);
				});
		}
		return players;
	}

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (!isEffectActive(gamemodeKey, player)) return;
		player.getPersistentDataContainer().set(gamemodeKey, BOOLEAN, false);
		player.setGameMode(GameMode.SURVIVAL);
    }

    // boilerplate stuff for the data container storage
	private static final class BooleanDataType implements PersistentDataType<Byte, Boolean> {
		private static final byte TRUE = 1;
		private static final byte FALSE = 0;

		@NotNull
		public Class<Byte> getPrimitiveType() {
			return Byte.class;
		}

		@NotNull
		public Class<Boolean> getComplexType() {
			return Boolean.class;
		}

		@NotNull
		public Byte toPrimitive(@NotNull Boolean complex, @NotNull PersistentDataAdapterContext context) {
			return complex ? TRUE : FALSE;
		}

		@NotNull
		public Boolean fromPrimitive(@NotNull Byte primitive, @NotNull PersistentDataAdapterContext context) {
			return primitive != FALSE;
		}
	}
}
