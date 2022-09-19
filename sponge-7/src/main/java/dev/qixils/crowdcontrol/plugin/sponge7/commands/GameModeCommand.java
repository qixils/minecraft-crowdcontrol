package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge7.TimedCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.data.entity.GameModeEffectData;
import dev.qixils.crowdcontrol.plugin.sponge7.utils.SpongeTextUtil;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Getter
public class GameModeCommand extends TimedCommand {
	private final Duration duration;
	private final GameMode gamemode;
	private final Component displayName;
	private final String effectName;

	public GameModeCommand(SpongeCrowdControlPlugin plugin, GameMode gamemode, long seconds) {
		super(plugin);
		this.duration = Duration.ofSeconds(seconds);
		this.gamemode = gamemode;
		this.displayName = Component.translatable(gamemode.getTranslation().getId());
		this.effectName = SpongeTextUtil.valueOf(gamemode) + "_mode";
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
					setGameMode(request, curPlayers, gamemode);
					players.addAll(curPlayers);
					playerAnnounce(players, request);
					return null;
				})
				.completionCallback($ -> setGameMode(null, players, GameModes.SURVIVAL))
				.build().queue();
	}

	private void setGameMode(@Nullable Request request,
							 @NotNull List<@NotNull Player> players,
							 @NotNull GameMode gamemode) {
		if (players.isEmpty())
			return;
		sync(() -> players.forEach(player -> {
			player.offer(Keys.GAME_MODE, gamemode);
			if (request == null)
				player.remove(GameModeEffectData.class);
			else
				player.offer(new GameModeEffectData(gamemode));
		}));
	}

	public static final class Manager {
		@Listener
		public void onJoin(ClientConnectionEvent.Join event) {
			Player player = event.getTargetEntity();
			if (!player.get(GameModeEffectData.class).isPresent()) return;
			player.remove(GameModeEffectData.class);
			player.offer(Keys.GAME_MODE, GameModes.SURVIVAL);
		}
	}

}
