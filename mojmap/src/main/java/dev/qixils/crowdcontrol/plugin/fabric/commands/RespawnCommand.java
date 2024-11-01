package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

@Getter
public class RespawnCommand extends ModdedCommand {
	private final String effectName = "respawn";
	private final List<String> effectGroups = List.of("walk", "look");

	public RespawnCommand(ModdedCrowdControlPlugin plugin) {
		super(plugin);
	}

	private void teleport(ServerPlayer player, ServerLevel level, BlockPos pos, float angle) {
		sync(() -> player.teleportTo(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, angle, 0));
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(() -> {
			if (isActive(ccPlayer, getEffectArray()))
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Cannot fling while frozen");
			boolean success = false;
			for (ServerPlayer player : playerSupplier.get()) {
				ServerLevel level = player.server.getLevel(player.getRespawnDimension());
				BlockPos pos = player.getRespawnPosition();
				float angle = player.getRespawnAngle();
				if (level == null || pos == null) {
					level = player.server.getLevel(Level.OVERWORLD);
					if (level == null)
						continue;
					pos = level.getSharedSpawnPos();
				}
				teleport(player, level, pos, angle);
				success = true;
			}
			return success
				? new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS)
				: new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Could not find a respawn point");
		}));
	}
}
