package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.TimedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.event.Join;
import dev.qixils.crowdcontrol.plugin.fabric.event.Listener;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.Components;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.GameTypeEffectComponent;
import dev.qixils.crowdcontrol.socket.Request;
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
public class GameModeCommand extends TimedCommand {
	private final Duration duration;
	private final GameType gamemode;
	private final Component displayName;
	private final String effectName;

	public GameModeCommand(FabricCrowdControlPlugin plugin, GameType gamemode, long seconds) {
		super(plugin);
		this.duration = Duration.ofSeconds(seconds);
		this.gamemode = gamemode;
		this.displayName = gamemode.getLongDisplayName().asComponent();
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
			GameTypeEffectComponent data = Components.GAME_TYPE_EFFECT.get(player);
			player.setGameMode(gamemode);
			if (request == null)
				data.setValue(null);
			else
				data.setValue(gamemode);
		}));
	}

	public static final class Manager {
		@SuppressWarnings("unused")
		@Listener
		public void onJoin(Join event) {
			ServerPlayer player = event.player();
			GameTypeEffectComponent data = Components.GAME_TYPE_EFFECT.get(player);
			GameType gameMode = data.getValue();
			if (gameMode == null) return;
			data.setValue(null);
			player.setGameMode(GameType.SURVIVAL);
		}
	}

}
