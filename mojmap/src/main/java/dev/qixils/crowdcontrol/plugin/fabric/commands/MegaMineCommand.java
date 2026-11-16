package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.EventListener;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.event.BlockBreak;
import dev.qixils.crowdcontrol.plugin.fabric.event.Listener;
import dev.qixils.crowdcontrol.plugin.fabric.event.Tick;
import dev.qixils.crowdcontrol.plugin.fabric.utils.Location;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.CCTimedEffect;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.CCTimedEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.http.CustomEffectDuration;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

@Getter
@EventListener
public class MegaMineCommand extends ModdedCommand implements CCTimedEffect {
	private final String effectName = "megamine";
	private final Map<UUID, List<UUID>> playerMap = new HashMap<>();
	private final CustomEffectDuration extensionDuration = new CustomEffectDuration(30);
	/** triggered this tick */
	private static final Set<UUID> TRIGGERED = new HashSet<>();

	public MegaMineCommand(ModdedCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			if (isActive(ccPlayer, getEffectArray()))
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Cannot stack effect");

			List<ServerPlayer> players = playerSupplier.get();
			playerMap.put(request.getRequestId(), players.stream().map(ServerPlayer::getUUID).toList());
			return new CCTimedEffectResponse(request.getRequestId(), ResponseStatus.TIMED_BEGIN, request.getEffect().getDurationMillis());
		}));
	}

	@Override
	public void onEnd(@NotNull PublicEffectPayload request, @NotNull CCPlayer source) {
		playerMap.remove(request.getRequestId());
	}

	@Listener
	public void onBlockBreak(BlockBreak event) {
		// easier short circuit; we'll add to it later
		if (TRIGGERED.contains(event.player().getUUID()))
			return;

		if (playerMap.values().stream().noneMatch(list -> list.contains(event.player().getUUID())))
			return;

		TRIGGERED.add(event.player().getUUID());

		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				for (int z = -1; z <= 1; z++) {
					if (x == 0 && y == 0 && z == 0) continue;

					Location nextBlockLoc = new Location(event.level(), event.blockPos()).add(x, y, z);
					if (nextBlockLoc.block().isAir()) continue;

					event.player().gameMode.destroyBlock(nextBlockLoc.pos());
				}
			}
		}
	}

	@Listener
	public void onTick(Tick event) {
		TRIGGERED.clear();
	}
}
