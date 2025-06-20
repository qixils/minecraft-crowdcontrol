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

import java.util.Collections;
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
		sync(() -> player.teleportTo(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, Collections.emptySet(), angle, 0, false)); // boolean is unused?
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			if (isActive(ccPlayer, getEffectArray()))
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Cannot fling while frozen");
			boolean success = false;
			for (ServerPlayer player : playerSupplier.get()) {
				ServerPlayer.RespawnConfig respawnConfig = player.getRespawnConfig();
				ServerLevel level;
				BlockPos pos;
				float angle;
				if (respawnConfig == null) {
					if (player.getServer() == null)
						continue;
					level = player.getServer().getLevel(Level.OVERWORLD);
					if (level == null)
						continue;
					pos = level.getSharedSpawnPos();
					angle = 0;
				} else {
					if (player.getServer() == null) continue;
					level = player.getServer().getLevel(respawnConfig.dimension());
					if (level == null) continue;
					pos = respawnConfig.pos();
					angle = respawnConfig.angle();
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
