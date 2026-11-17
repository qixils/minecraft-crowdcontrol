package dev.qixils.crowdcontrol.plugin.paper.commands;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.paper.PaperCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.CCTimedEffect;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.CCTimedEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.http.CustomEffectDuration;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

@Getter
public class MegaMineCommand extends PaperCommand implements CCTimedEffect, Listener {
	private final String effectName = "megamine";
	private final Map<UUID, List<UUID>> playerMap = new HashMap<>();
	private final CustomEffectDuration extensionDuration = new CustomEffectDuration(30);
	/** triggered this tick */
	private static final Set<UUID> TRIGGERED = new HashSet<>();

	public MegaMineCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull Player>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			if (isActive(ccPlayer, getEffectArray()))
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Cannot stack effect");

			List<Player> players = playerSupplier.get();

			playerMap.put(request.getRequestId(), players.stream().map(Player::getUniqueId).toList());

			return new CCTimedEffectResponse(request.getRequestId(), ResponseStatus.TIMED_BEGIN, request.getEffect().getDurationMillis());
		}));
	}

	@Override
	public void onEnd(@NotNull PublicEffectPayload request, @NotNull CCPlayer source) {
		playerMap.remove(request.getRequestId());
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		// easier short circuit; we'll add to it later
		if (TRIGGERED.contains(event.getPlayer().getUniqueId()))
			return;

		if (playerMap.values().stream().noneMatch(list -> list.contains(event.getPlayer().getUniqueId())))
			return;

		TRIGGERED.add(event.getPlayer().getUniqueId());

		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				for (int z = -1; z <= 1; z++) {
					if (x == 0 && y == 0 && z == 0) continue;

					Block nextBlock = event.getBlock().getLocation().add(x, y, z).getBlock();
					if (nextBlock.isEmpty()) continue;

					event.getPlayer().breakBlock(nextBlock);
				}
			}
		}
	}

	@EventHandler
	public void onTick(ServerTickStartEvent event) {
		TRIGGERED.clear();
	}
}
