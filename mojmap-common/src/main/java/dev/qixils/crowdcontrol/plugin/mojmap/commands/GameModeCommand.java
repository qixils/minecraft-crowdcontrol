package dev.qixils.crowdcontrol.plugin.mojmap.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.mojmap.MojmapPlugin;
import dev.qixils.crowdcontrol.plugin.mojmap.TimedCommand;
import dev.qixils.crowdcontrol.plugin.mojmap.event.Join;
import dev.qixils.crowdcontrol.plugin.mojmap.event.Listener;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static dev.qixils.crowdcontrol.plugin.mojmap.MojmapPlugin.GAME_MODE_EFFECT;

@Getter
public class GameModeCommand extends TimedCommand {
	private final Duration duration;
	private final GameType gamemode;
	private final String displayName;
	private final String effectName;

	public GameModeCommand(MojmapPlugin plugin, GameType gamemode, long seconds) {
		super(plugin);
		this.duration = Duration.ofSeconds(seconds);
		this.gamemode = gamemode;
		this.displayName = plugin.getTextUtil().asPlain(gamemode.getShortDisplayName());
		this.effectName = gamemode.getName() + "_mode";
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull ServerPlayer> ignored, @NotNull Request request) {
		List<ServerPlayer> players = new ArrayList<>();

		new TimedEffect.Builder()
				.request(request)
				.effectGroup("gamemode")
				.duration(duration)
				.startCallback($ -> {
					List<ServerPlayer> curPlayers = plugin.getPlayers(request);
					setGameMode(request, curPlayers, gamemode);
					players.addAll(curPlayers);
					playerAnnounce(players, request);
					return null;
				})
				.completionCallback($ -> setGameMode(null, players, GameType.SURVIVAL))
				.build().queue();
	}

	private void setGameMode(@Nullable Request request,
							 @NotNull List<@NotNull ServerPlayer> players,
							 @NotNull GameType gamemode) {
		if (players.isEmpty())
			return;
		sync(() -> players.forEach(player -> {
			player.setGameMode(gamemode);
			if (request == null)
				player.getEntityData().set(GAME_MODE_EFFECT, "");
			else
				player.getEntityData().set(GAME_MODE_EFFECT, gamemode.getName());
		}));
	}

	public static final class Manager {
		@Listener
		public void onJoin(Join event) {
			ServerPlayer player = event.player();
			String gamemodeString = player.getEntityData().get(GAME_MODE_EFFECT);
			if (gamemodeString == null) return;
			if (gamemodeString.isEmpty()) return;
			player.getEntityData().set(GAME_MODE_EFFECT, "");
			player.setGameMode(GameType.SURVIVAL);
		}
	}

}
