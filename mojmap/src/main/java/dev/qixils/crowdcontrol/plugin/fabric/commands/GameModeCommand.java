package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.event.Join;
import dev.qixils.crowdcontrol.plugin.fabric.event.Listener;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.CCTimedEffect;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.CCTimedEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;

@Getter
public class GameModeCommand extends ModdedCommand implements CCTimedEffect {
	private final Duration defaultDuration;
	private final GameType gamemode;
	private final Component displayName;
	private final String effectName;

	private final Map<UUID, List<UUID>> activeRequests = new HashMap<>();
	private final String effectGroup = "gamemode";
	private final List<String> effectGroups = Collections.singletonList(effectGroup);

	public GameModeCommand(ModdedCrowdControlPlugin plugin, GameType gamemode, long seconds) {
		super(plugin);
		this.defaultDuration = Duration.ofSeconds(seconds);
		this.gamemode = gamemode;
		this.displayName = plugin.toAdventure(gamemode.getLongDisplayName());
		this.effectName = gamemode.getName() + "_mode";
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(() -> {
			if (isActive(ccPlayer, getEffectArray()))
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Conflicting effects active");
			List<ServerPlayer> players = playerSupplier.get();
			activeRequests.put(request.getRequestId(), players.stream().map(ServerPlayer::getUUID).toList());
			setGameMode(players, gamemode, true);
			return new CCTimedEffectResponse(request.getRequestId(), ResponseStatus.TIMED_BEGIN, request.getEffect().getDuration() * 1000L);
		}, plugin.getSyncExecutor()));
	}

	@Override
	public void onEnd(@NotNull PublicEffectPayload request, @NotNull CCPlayer source) {
		List<ServerPlayer> players = plugin.toPlayerList(activeRequests.remove(request.getRequestId()));
		setGameMode(players, GameType.SURVIVAL, false);
	}

	private void setGameMode(@NotNull List<@NotNull ServerPlayer> players,
							 @NotNull GameType gamemode,
							 boolean enabling) {
		if (players.isEmpty())
			return;
		sync(() -> players.forEach(player -> {
			player.setGameMode(gamemode);
			player.cc$setGameTypeEffect(enabling ? gamemode : null);
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
