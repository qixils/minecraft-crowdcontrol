package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Respondable;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.KEEP_INVENTORY_MESSAGE;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.LOSE_INVENTORY_MESSAGE;
import static dev.qixils.crowdcontrol.common.util.sound.Sounds.KEEP_INVENTORY_ALERT;
import static dev.qixils.crowdcontrol.common.util.sound.Sounds.LOSE_INVENTORY_ALERT;

@Getter
public class KeepInventoryCommand extends ImmediateCommand {
	private static final Set<UUID> keepingInventory = Collections.synchronizedSet(new HashSet<>(1));
	public static boolean globalKeepInventory = false;
	private final boolean enable;
	private final String effectName;

	public KeepInventoryCommand(ModdedCrowdControlPlugin plugin, boolean enable) {
		super(plugin);
		this.enable = enable;
		this.effectName = "keep_inventory_" + (enable ? "on" : "off");
	}

	public static boolean isKeepingInventory(UUID player) {
		return globalKeepInventory || keepingInventory.contains(player);
	}

	public static boolean isKeepingInventory(Entity player) {
		return isKeepingInventory(player.getUUID());
	}

	private void alert(List<ServerPlayer> players) {
		Audience audience = plugin.playerMapper().asAudience(players);
		audience.sendActionBar(enable ? KEEP_INVENTORY_MESSAGE : LOSE_INVENTORY_MESSAGE);
		audience.playSound((enable ? KEEP_INVENTORY_ALERT : LOSE_INVENTORY_ALERT).get(), Sound.Emitter.self());
	}

	private void updateEffectVisibility(@Nullable Respondable respondable) {
		async(() -> {
			plugin.updateEffectStatus(respondable, ResultType.NOT_SELECTABLE, effectName);
			plugin.updateEffectStatus(respondable, ResultType.SELECTABLE, "keep_inventory_" + (!enable ? "on" : "off"));
			plugin.updateEffectStatus(respondable, enable ? ResultType.NOT_SELECTABLE : ResultType.SELECTABLE, "clear_inventory");
		});
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder resp = request.buildResponse();

		if (plugin.isGlobal()) {
			if (globalKeepInventory == enable) {
				return resp.type(ResultType.FAILURE).message("Keep Inventory is already " + (enable ? "enabled" : "disabled"));
			}
			globalKeepInventory = enable;
			alert(players);
			updateEffectVisibility(plugin.getCrowdControl());
			return resp.type(ResultType.SUCCESS);
		}

		List<UUID> uuids = new ArrayList<>(players.size());
		for (ServerPlayer player : players)
			uuids.add(player.getUUID());

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
		if (!plugin.isGlobal())
			return TriState.TRUE;
		if (globalKeepInventory == enable)
			return TriState.FALSE;
		return TriState.TRUE;
	}

	// management of this command is handled by mixins
}
