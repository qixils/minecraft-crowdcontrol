package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.TimedVoidCommand;
import dev.qixils.crowdcontrol.plugin.fabric.event.Join;
import dev.qixils.crowdcontrol.plugin.fabric.event.Listener;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.Components;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.GameTypeEffectComponent;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Getter
public class GameModeCommand extends TimedVoidCommand {
	private final Duration defaultDuration;
	private final GameMode gamemode;
	private final Component displayName;
	private final String effectName;

	public GameModeCommand(FabricCrowdControlPlugin plugin, GameMode gamemode, long seconds) {
		super(plugin);
		this.defaultDuration = Duration.ofSeconds(seconds);
		this.gamemode = gamemode;
		this.displayName = gamemode.getTranslatableName().asComponent();
		this.effectName = gamemode.getName() + "_mode";
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull ServerPlayerEntity> ignored, @NotNull Request request) {
		List<ServerPlayerEntity> players = new ArrayList<>();

		new TimedEffect.Builder()
				.request(request)
				.effectGroup("gamemode")
				.duration(getDuration(request))
				.startCallback($ -> {
					List<ServerPlayerEntity> curPlayers = plugin.getPlayers(request);
					setGameMode(request, curPlayers, gamemode);
					players.addAll(curPlayers);
					playerAnnounce(players, request); // sometimes duplicates after death but I think this is exclusive to the dev environment
					return null;
				})
				.completionCallback($ -> setGameMode(null, players, GameMode.SURVIVAL))
				.build().queue();
	}

	private void setGameMode(@Nullable Request request,
							 @NotNull List<@NotNull ServerPlayerEntity> players,
							 @NotNull GameMode gamemode) {
		if (players.isEmpty())
			return;
		sync(() -> players.forEach(player -> {
			GameTypeEffectComponent data = Components.GAME_TYPE_EFFECT.get(player);
			player.changeGameMode(gamemode);
			if (request == null)
				data.setValue(null);
			else
				data.setValue(gamemode);
		}));
	}

	public static final class Manager {
		@Listener
		public void onJoin(Join event) {
			ServerPlayerEntity player = event.player();
			GameTypeEffectComponent data = Components.GAME_TYPE_EFFECT.get(player);
			GameMode gameMode = data.getValue();
			if (gameMode == null) return;
			data.setValue(null);
			player.changeGameMode(GameMode.SURVIVAL);
		}
	}

}
