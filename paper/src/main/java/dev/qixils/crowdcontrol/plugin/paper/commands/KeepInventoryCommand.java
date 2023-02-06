package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Respondable;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.KEEP_INVENTORY_MESSAGE;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.LOSE_INVENTORY_MESSAGE;
import static dev.qixils.crowdcontrol.common.util.sound.Sounds.KEEP_INVENTORY_ALERT;
import static dev.qixils.crowdcontrol.common.util.sound.Sounds.LOSE_INVENTORY_ALERT;

// TODO most of this code is duplicated across all the plugins

@Getter
public class KeepInventoryCommand extends ImmediateCommand {
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
		Audience audience = getPlugin().translator().wrap(players);
		audience.sendActionBar(enable ? KEEP_INVENTORY_MESSAGE : LOSE_INVENTORY_MESSAGE);
		audience.playSound((enable ? KEEP_INVENTORY_ALERT : LOSE_INVENTORY_ALERT).get(), Sound.Emitter.self());
	}

	private void updateEffectVisibility(@Nullable Respondable respondable) { // TODO: add to other impls (and check like difficulty too ig?)
		async(() -> {
			plugin.updateEffectStatus(respondable, effectName, ResultType.NOT_SELECTABLE);
			plugin.updateEffectStatus(respondable, "keep_inventory_" + (!enable ? "on" : "off"), ResultType.SELECTABLE);
			plugin.updateEffectStatus(respondable, "clear_inventory", enable ? ResultType.NOT_SELECTABLE : ResultType.SELECTABLE);
		});
	}

	@Override
	@NotNull
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder resp = request.buildResponse();

		if (isGlobalCommandUsable(players, request)) {
			if (globalKeepInventory == enable) {
				return resp.type(ResultType.FAILURE).message("Keep Inventory is already " + (enable ? "enabled" : "disabled"));
			}
			globalKeepInventory = enable;
			alert(players);
			updateEffectVisibility(plugin.getCrowdControl());
			return resp.type(ResultType.SUCCESS);
		}

		List<UUID> uuids = new ArrayList<>(players.size());
		for (Player player : players)
			uuids.add(player.getUniqueId());

		if (enable) {
			if (keepingInventory.addAll(uuids)) {
				alert(players);
				//updateEffectVisibility(request);
				return resp.type(ResultType.SUCCESS);
			} else
				return resp.type(ResultType.FAILURE).message("Streamer(s) already have Keep Inventory enabled");
		} else {
			if (keepingInventory.removeAll(uuids)) {
				alert(players);
				//updateEffectVisibility(request);
				return resp.type(ResultType.SUCCESS);
			} else
				return resp.type(ResultType.FAILURE).message("Streamer(s) already have Keep Inventory disabled");
		}
	}

	@Override
	public TriState isSelectable() {
		if (!plugin.globalEffectsUsable())
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
