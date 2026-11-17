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
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Getter
public class FloorIsLavaCommand extends PaperCommand implements CCTimedEffect, Listener {
	private final String effectName = "floor_is_lava";
	private final Map<UUID, List<UUID>> playerMap = new HashMap<>();
	private final CustomEffectDuration extensionDuration = new CustomEffectDuration(30);
	private static final Set<Material> LAVA_BLOCKS = Set.of(
		Material.ANDESITE,
		Material.DIORITE,
		Material.GRANITE,
		Material.STONE,
		Material.GRASS_BLOCK,
		Material.DIRT,
		Material.NETHERRACK,
		Material.END_STONE,
		Material.MYCELIUM,
		Material.SAND,
		Material.GRAVEL
	);

	public FloorIsLavaCommand(PaperCrowdControlPlugin plugin) {
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
	public void onTick(ServerTickStartEvent event) {
		Set<UUID> playerIds = playerMap.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
		for (UUID uuid : playerIds) {
			Player player = Bukkit.getPlayer(uuid);
			if (player == null) continue;
			// TODO: replace? i don't see a suitable api at a glance
			if (!player.isOnGround()) continue;

			var pos = player.getLocation();
			var block = player.getWorld().getBlockAt(pos.getBlockX(), pos.getBlockY() - 1, pos.getBlockZ()).getType();
			if (!LAVA_BLOCKS.contains(block)) continue;

			player.setFireTicks(Math.max(player.getFireTicks(), 20));
		}
	}
}
