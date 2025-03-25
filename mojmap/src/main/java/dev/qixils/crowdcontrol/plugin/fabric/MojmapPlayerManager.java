package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.common.AbstractPlayerManager;
import dev.qixils.crowdcontrol.common.Plugin;
import dev.qixils.crowdcontrol.common.util.PermissionWrapper;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public class MojmapPlayerManager extends AbstractPlayerManager<ServerPlayer> {
	private final ModdedCrowdControlPlugin plugin;

	@Override
	public @NotNull List<@NotNull ServerPlayer> getAllPlayersFull() {
		return new ArrayList<>(getPlugin().server().getPlayerList().getPlayers());
	}

	@Override
	public boolean canApply(@NotNull ServerPlayer player, @Nullable PublicEffectPayload request) {
		if (player.isDeadOrDying()) return false;
		if (isSpectator(player)) return false;
		if (!plugin.getPermissionUtil().check(player, Plugin.USE_PERMISSION)) return false;
		PermissionWrapper perm = getEffectPermission(request).orElse(null);
		if (perm != null && !plugin.getPermissionUtil().check(player, perm)) return false;
		return true;
	}

	@Override
	public boolean isSpectator(@NotNull ServerPlayer player) {
		return player.isSpectator() && player.cc$getGameTypeEffect() == null;
	}
}
