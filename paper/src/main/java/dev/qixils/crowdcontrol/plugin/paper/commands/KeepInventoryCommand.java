package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.paper.PaperCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.CrowdControl;
import live.crowdcontrol.cc4j.IUserRecord;
import live.crowdcontrol.cc4j.websocket.data.CCEffectReport;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ReportStatus;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.KEEP_INVENTORY_MESSAGE;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.LOSE_INVENTORY_MESSAGE;
import static dev.qixils.crowdcontrol.common.util.sound.Sounds.KEEP_INVENTORY_ALERT;
import static dev.qixils.crowdcontrol.common.util.sound.Sounds.LOSE_INVENTORY_ALERT;

// TODO most of this code is duplicated across all the plugins

@Getter
public class KeepInventoryCommand extends PaperCommand {
	private static final Set<UUID> keepingInventory = Collections.synchronizedSet(new HashSet<>(1));
	private final boolean enable;
	private final String effectName;
	public static boolean globalKeepInventory = false;

	public KeepInventoryCommand(PaperCrowdControlPlugin plugin, boolean enable) {
		super(plugin);
		this.enable = enable;
		this.effectName = "keep_inventory_" + (enable ? "on" : "off");
	}

	public static boolean isKeepingInventory(UUID player) {
		return globalKeepInventory || keepingInventory.contains(player);
	}

	public static boolean isKeepingInventory(Entity player) {
		return isKeepingInventory(player.getUniqueId());
	}

	private void alert(Collection<? extends Audience> players) {
		Audience audience = Audience.audience(players);
		audience.sendActionBar(enable ? KEEP_INVENTORY_MESSAGE : LOSE_INVENTORY_MESSAGE);
		audience.playSound((enable ? KEEP_INVENTORY_ALERT : LOSE_INVENTORY_ALERT).get(), Sound.Emitter.self());
	}

	private void updateEffectVisibility(@NotNull CCPlayer ccPlayer) {
		// TODO: optimize
		ccPlayer.sendReport(
			new CCEffectReport(ReportStatus.MENU_UNAVAILABLE, effectName),
			new CCEffectReport(ReportStatus.MENU_AVAILABLE, "keep_inventory_" + (!enable ? "on" : "off")),
			new CCEffectReport(enable ? ReportStatus.MENU_UNAVAILABLE : ReportStatus.MENU_AVAILABLE, "clear_inventory")
		);
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull Player>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(() -> {
			List<Player> players = playerSupplier.get();

			if (plugin.isGlobal()) {
				CrowdControl cc = plugin.getCrowdControl();
				if (cc == null) return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Mod is not loaded");
				if (globalKeepInventory == enable) {
					return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Keep Inventory is already " + (enable ? "enabled" : "disabled"));
				}
				globalKeepInventory = enable;
				alert(players);
				cc.getPlayers().forEach(this::updateEffectVisibility);
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS);
			}

			List<UUID> uuids = new ArrayList<>(players.size());
			for (Player player : players)
				uuids.add(player.getUniqueId());

			if (enable) {
				if (keepingInventory.addAll(uuids)) {
					alert(players);
					updateEffectVisibility(ccPlayer);
					return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS);
				} else
					return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Streamer(s) already have Keep Inventory enabled");
			} else {
				if (keepingInventory.removeAll(uuids)) {
					alert(players);
					updateEffectVisibility(ccPlayer);
					return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS);
				} else
					return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Streamer(s) already have Keep Inventory disabled");
			}
		}));
	}

	@Override
	public TriState isSelectable(@NotNull IUserRecord user, @NotNull List<Player> potentialPlayers) {
		if (!plugin.isGlobal())
			return TriState.TRUE;
		if (globalKeepInventory == enable)
			return TriState.FALSE;
		return TriState.TRUE;
	}

	public static final class Manager implements Listener {
		@EventHandler
		public void onDeath(PlayerDeathEvent event) {
			if (!isKeepingInventory(event.getEntity()))
				return;
			event.setKeepInventory(true);
			event.getDrops().clear();
			event.setKeepLevel(true);
			event.setDroppedExp(0);
		}
	}
}
