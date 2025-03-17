package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.paper.PaperCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.CrowdControl;
import live.crowdcontrol.cc4j.IUserRecord;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import org.bukkit.GameRule;
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
				plugin.updateConditionalEffectVisibility();
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS);
			}

			List<UUID> uuids = new ArrayList<>(players.size());
			for (Player player : players)
				uuids.add(player.getUniqueId());

			if (enable) {
				if (keepingInventory.addAll(uuids)) {
					alert(players);
					players.forEach(plugin::updateConditionalEffectVisibility);
					return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS);
				} else
					return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Streamer(s) already have Keep Inventory enabled");
			} else {
				if (keepingInventory.removeAll(uuids)) {
					alert(players);
					players.forEach(plugin::updateConditionalEffectVisibility);
					return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS);
				} else
					return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Streamer(s) already have Keep Inventory disabled");
			}
		}));
	}

	@Override
	public TriState isVisible(@NotNull IUserRecord user, @NotNull List<Player> potentialPlayers) {
		// Cannot use inventory effects while /gamerule keepInventory true
		return potentialPlayers.stream()
			.anyMatch(player -> player.getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY) == Boolean.TRUE)
			? TriState.FALSE
			: TriState.TRUE;
	}

	@Override
	public TriState isSelectable(@NotNull IUserRecord user, @NotNull List<Player> potentialPlayers) {
		if (plugin.isGlobal())
			return globalKeepInventory == enable ? TriState.FALSE : TriState.TRUE;

		TriState state = potentialPlayers.stream()
			.map(player -> TriState.fromBoolean(enable != isKeepingInventory(player.getUniqueId())))
			.reduce((prev, next) -> {
				if (prev != next) return TriState.UNKNOWN;
				return prev;
			})
			.orElse(TriState.UNKNOWN);

		if (state == TriState.FALSE) return state;
		return TriState.TRUE; // fixing UNKNOWN essentially
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
