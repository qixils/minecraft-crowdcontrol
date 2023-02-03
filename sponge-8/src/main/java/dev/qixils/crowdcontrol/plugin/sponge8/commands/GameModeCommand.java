package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge8.TimedVoidCommand;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.registry.RegistryTypes;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin.GAME_MODE_EFFECT;

@Getter
public class GameModeCommand extends TimedVoidCommand {
	private final Duration defaultDuration;
	private final GameMode gamemode;
	private final Component displayName;
	private final String effectName;

	public GameModeCommand(SpongeCrowdControlPlugin plugin, GameMode gamemode, long seconds) {
		super(plugin);
		this.defaultDuration = Duration.ofSeconds(seconds);
		this.gamemode = gamemode;
		this.displayName = gamemode.asComponent();
		this.effectName = gamemode.key(RegistryTypes.GAME_MODE).value() + "_mode";
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
					playerAnnounce(players, request);
					return null;
				})
				.completionCallback($ -> setGameMode(null, players, GameModes.SURVIVAL.get()))
				.build().queue();
	}

	private void setGameMode(@Nullable Request request,
							 @NotNull List<@NotNull ServerPlayer> players,
							 @NotNull GameMode gamemode) {
		if (players.isEmpty())
			return;
		sync(() -> players.forEach(player -> {
			player.offer(Keys.GAME_MODE, gamemode);
			if (request == null)
				player.remove(GAME_MODE_EFFECT);
			else
				player.offer(GAME_MODE_EFFECT, gamemode.key(RegistryTypes.GAME_MODE));
		}));
	}

	public static final class Manager {
		@Listener
		public void onJoin(ServerSideConnectionEvent.Join event) {
			ServerPlayer player = event.player();
			if (!player.get(GAME_MODE_EFFECT).isPresent()) return;
			player.remove(GAME_MODE_EFFECT);
			player.offer(Keys.GAME_MODE, GameModes.SURVIVAL.get());
		}
	}

}
