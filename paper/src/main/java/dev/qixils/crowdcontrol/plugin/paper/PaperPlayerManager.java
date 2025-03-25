package dev.qixils.crowdcontrol.plugin.paper;

import dev.qixils.crowdcontrol.common.AbstractPlayerManager;
import dev.qixils.crowdcontrol.plugin.paper.commands.GameModeCommand;
import dev.qixils.crowdcontrol.plugin.paper.utils.PaperUtil;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static dev.qixils.crowdcontrol.plugin.paper.utils.PaperUtil.hasPermission;

@Getter
@RequiredArgsConstructor
public final class PaperPlayerManager extends AbstractPlayerManager<Player> {
	private final PaperCrowdControlPlugin plugin;

	@Override
	public @NotNull List<@NotNull Player> getAllPlayersFull() {
		return new ArrayList<>(Bukkit.getOnlinePlayers());
	}

	@Override
	public boolean canApply(@NotNull Player player, @Nullable PublicEffectPayload request) {
		if (!player.isValid()) return false;
		if (player.isDead()) return false;
		if (isSpectator(player)) return false;
		if (!hasPermission(player, PaperUtil.USE_PERMISSION)) return false;
		Permission perm = getEffectPermission(request).map(PaperUtil::toPaper).orElse(null);
		if (perm != null && !hasPermission(player, perm)) return false;
		return true;
	}

	@Override
	public boolean isSpectator(@NotNull Player player) {
		return player.getGameMode() == GameMode.SPECTATOR && !GameModeCommand.isEffectActive(plugin.getPaperPlugin(), player);
	}
}
