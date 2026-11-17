package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.packets.LidarPacketS2C;
import dev.qixils.crowdcontrol.common.util.SemVer;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.packets.LidarS2C;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.CCTimedEffect;
import live.crowdcontrol.cc4j.websocket.data.CCTimedEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

@Getter
public class LidarCommand extends ModdedCommand implements CCTimedEffect {
	private final @NotNull String effectName = "lidar";
	private final @NotNull Duration defaultDuration = Duration.ofSeconds(60);
	private final @NotNull SemVer minimumModVersion = LidarPacketS2C.ADDED_IN;

	private final Map<UUID, List<UUID>> uuidMap = new HashMap<>();

	public LidarCommand(@NotNull ModdedCrowdControlPlugin plugin) {
		super(plugin);
	}

	/**
	 * LiDAR paints over the whole screen, so shaders can't run alongside it
	 */
	@Override
	public String[] getEffectArray() {
		return new String[]{"shaders", effectName};
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			List<ServerPlayer> players = playerSupplier.get();
			uuidMap.put(request.getRequestId(), players.stream().map(ServerPlayer::getUUID).toList());
			send(players, Duration.ofMillis(request.getEffect().getDurationMillis()));
			return new CCTimedEffectResponse(request.getRequestId(), ResponseStatus.TIMED_BEGIN, request.getEffect().getDurationMillis());
		}));
	}

	@Override
	public void onPause(@NotNull PublicEffectPayload request, @NotNull CCPlayer source) {
		sendToIds(uuidMap.get(request.getRequestId()), Duration.ZERO);
	}

	@Override
	public void onResume(@NotNull PublicEffectPayload request, @NotNull CCPlayer source) {
		// the client only uses this as a fallback timeout, so the full duration is a safe upper bound
		sendToIds(uuidMap.get(request.getRequestId()), Duration.ofMillis(request.getEffect().getDurationMillis()));
	}

	@Override
	public void onEnd(@NotNull PublicEffectPayload request, @NotNull CCPlayer source) {
		sendToIds(uuidMap.remove(request.getRequestId()), Duration.ZERO);
	}

	private void sendToIds(@Nullable List<UUID> uuids, @NotNull Duration duration) {
		send(plugin.toPlayerList(uuids), duration);
	}

	private void send(@NotNull List<ServerPlayer> players, @NotNull Duration duration) {
		LidarS2C packet = new LidarS2C(duration);
		for (ServerPlayer player : players)
			plugin.sendToPlayer(player, packet);
	}
}
