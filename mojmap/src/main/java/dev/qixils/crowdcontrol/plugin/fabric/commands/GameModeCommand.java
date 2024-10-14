package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.TimedVoidCommand;
import dev.qixils.crowdcontrol.plugin.fabric.event.Join;
import dev.qixils.crowdcontrol.plugin.fabric.event.Listener;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Getter
public class GameModeCommand extends TimedVoidCommand {
	private final Duration defaultDuration;
	private final GameType gamemode;
	private final Component displayName;
	private final String effectName;

	public GameModeCommand(ModdedCrowdControlPlugin plugin, GameType gamemode, long seconds) {
		super(plugin);
		this.defaultDuration = Duration.ofSeconds(seconds);
		this.gamemode = gamemode;
		this.displayName = plugin.toAdventure(gamemode.getLongDisplayName());
		this.effectName = gamemode.getName() + "_mode";
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull ServerPlayer> ignored, @NotNull Request request) {
		List<ServerPlayer> players = new ArrayList<>();

		new TimedEffect.Builder()
				.request(request)
				.effectGroup("gamemode")
				.duration(getDuration(request))
				.startCallback($ -> {
					List<ServerPlayer> curPlayers = plugin.getPlayers(request);
					setGameMode(request, curPlayers, gamemode);
					players.addAll(curPlayers);
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
				player.cc$setGameTypeEffect(null);
			else {
				player.cc$setGameTypeEffect(gamemode);
				playerAnnounce(players, request);
			}
		}));
	}

	public static final class Manager {
		@Listener
		public void onJoin(Join event) {
			ServerPlayer player = event.player();
			GameType gameMode = player.cc$getGameTypeEffect();
			if (gameMode == null) return;
			player.cc$setGameTypeEffect(null);
			player.setGameMode(GameType.SURVIVAL);
		}
	}

}
