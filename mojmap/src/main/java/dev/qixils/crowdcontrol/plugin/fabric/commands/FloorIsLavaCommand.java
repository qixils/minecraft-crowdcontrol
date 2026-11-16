package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.EventListener;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.event.Listener;
import dev.qixils.crowdcontrol.plugin.fabric.event.Tick;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.CCTimedEffect;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.CCTimedEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.http.CustomEffectDuration;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Getter
@EventListener
public class FloorIsLavaCommand extends ModdedCommand implements CCTimedEffect {
	private final String effectName = "floor_is_lava";
	private final Map<UUID, List<UUID>> playerMap = new HashMap<>();
	private final CustomEffectDuration extensionDuration = new CustomEffectDuration(30);
	private static final Set<Block> LAVA_BLOCKS = Set.of(
		Blocks.ANDESITE,
		Blocks.DIORITE,
		Blocks.GRANITE,
		Blocks.STONE,
		Blocks.GRASS_BLOCK,
		Blocks.DIRT,
		Blocks.NETHERRACK,
		Blocks.END_STONE,
		Blocks.MYCELIUM,
		Blocks.SAND,
		Blocks.GRAVEL
	);

	public FloorIsLavaCommand(ModdedCrowdControlPlugin plugin) {
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
	public void onTick(Tick event) {
		Set<UUID> playerIds = playerMap.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
		for (UUID uuid : playerIds) {
			ServerPlayer player = plugin.theGame().playerList().getPlayer(uuid);
			if (player == null) continue;
			// TODO: replace? see paper
			if (!player.onGround()) continue;

			var pos = player.position();
			var block = player.level().getBlockState(BlockPos.containing(pos.x(), pos.y() - 1, pos.z())).getBlock();
			if (!LAVA_BLOCKS.contains(block)) continue;

			player.setRemainingFireTicks(Math.max(player.getRemainingFireTicks(), 20));
		}
	}
}
