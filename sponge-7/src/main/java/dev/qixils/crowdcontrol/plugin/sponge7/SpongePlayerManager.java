package dev.qixils.crowdcontrol.plugin.sponge7;

import dev.qixils.crowdcontrol.common.AbstractPlayerManager;
import dev.qixils.crowdcontrol.common.Plugin;
import dev.qixils.crowdcontrol.common.util.PermissionWrapper;
import dev.qixils.crowdcontrol.plugin.sponge7.data.entity.GameModeEffectData;
import dev.qixils.crowdcontrol.plugin.sponge7.utils.SpongeUtil;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;

import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class SpongePlayerManager extends AbstractPlayerManager<Player> {
	private final SpongeCrowdControlPlugin plugin;

	@Override
	public @NotNull List<@NotNull Player> getAllPlayersFull() {
		return new ArrayList<>(plugin.getGame().getServer().getOnlinePlayers());
	}

	@Override
	public boolean canApply(@NotNull Player player, @Nullable PublicEffectPayload request) {
		if (player.isRemoved()) return false;
		if (!player.isOnline()) return false;
		if (!player.isLoaded()) return false;
		if (player.health().get() <= 0.0) return false;
		if (isSpectator(player)) return false;
		if (!SpongeUtil.hasPermission(player, Plugin.USE_PERMISSION)) return false;
		PermissionWrapper perm = getEffectPermission(request).orElse(null);
		if (perm != null && !SpongeUtil.hasPermission(player, perm)) return false;
		return true;
	}

	@Override
	public boolean isSpectator(@NotNull Player player) {
		return player.gameMode().get().equals(GameModes.SPECTATOR) && !player.get(GameModeEffectData.class).isPresent();
	}
}
