package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.EventListener;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;

@Getter
@EventListener
public class FlightCommand extends ModdedCommand implements CCTimedEffect {
	private final String effectName = "flight";
	private final Duration defaultDuration = Duration.ofSeconds(15);

	private final String effectGroup = "gamemode";
	private final List<String> effectGroups = Collections.singletonList(effectGroup);

	private final Map<UUID, List<UUID>> uuidMap = new HashMap<>();

	public FlightCommand(@NotNull ModdedCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			List<ServerPlayer> players = playerSupplier.get();
			boolean success = false;
			for (ServerPlayer player : players) {
				GameType gamemode = player.gameMode.getGameModeForPlayer();
				if (gamemode == GameType.CREATIVE)
					continue;
				if (gamemode == GameType.SPECTATOR)
					continue;
				Abilities abilities = player.getAbilities();
				if (abilities.mayfly)
					continue;
				if (abilities.flying)
					continue;
				success = true;
				sync(() -> {
					abilities.mayfly = true;
					abilities.flying = true;
					player.addDeltaMovement(new Vec3(0, 0.2, 0));
					player.hurtMarked = true;
					player.onUpdateAbilities();
					// TODO: set abilities.flying=true; again after 1 tick
				});
			}
			if (!success)
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Target is already flying or able to fly");
			uuidMap.put(request.getRequestId(), players.stream().map(ServerPlayer::getUUID).toList());
			return new CCTimedEffectResponse(request.getRequestId(), ResponseStatus.TIMED_BEGIN, request.getEffect().getDurationMillis());
		}));
	}

	@Override
	public void onEnd(@NotNull PublicEffectPayload request, @NotNull CCPlayer source) {
		List<ServerPlayer> players = plugin.toPlayerList(uuidMap.remove(request.getRequestId()));
		sync(() -> players.forEach(player -> {
			Abilities abilities = player.getAbilities();
			abilities.mayfly = false;
			abilities.flying = false;
			player.onUpdateAbilities();
		}));
	}

	// clear flight on login if they disconnected mid-effect
	@Listener
	public void onJoin(Join event) {
		ServerPlayer player = event.player();
		GameType gamemode = player.gameMode.getGameModeForPlayer();
		if (gamemode == GameType.CREATIVE)
			return;
		if (gamemode == GameType.SPECTATOR)
			return;
		Abilities abilities = player.getAbilities();
		if (!abilities.flying && !abilities.mayfly)
			return;
		abilities.mayfly = false;
		abilities.flying = false;
		player.onUpdateAbilities();
	}
}
