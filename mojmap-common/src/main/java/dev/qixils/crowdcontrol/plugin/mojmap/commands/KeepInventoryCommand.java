package dev.qixils.crowdcontrol.plugin.mojmap.commands;

import dev.qixils.crowdcontrol.plugin.mojmap.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.mojmap.MojmapPlugin;
import dev.qixils.crowdcontrol.plugin.mojmap.event.Death;
import dev.qixils.crowdcontrol.plugin.mojmap.event.Listener;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static dev.qixils.crowdcontrol.common.CommandConstants.KEEP_INVENTORY_MESSAGE;
import static dev.qixils.crowdcontrol.common.CommandConstants.LOSE_INVENTORY_MESSAGE;
import static dev.qixils.crowdcontrol.common.util.sound.Sounds.KEEP_INVENTORY_ALERT;
import static dev.qixils.crowdcontrol.common.util.sound.Sounds.LOSE_INVENTORY_ALERT;

@Getter
public class KeepInventoryCommand extends ImmediateCommand {
	private static final Set<UUID> keepingInventory = Collections.synchronizedSet(new HashSet<>(1));
	private final boolean enable;
	private final String effectName;
	private final String displayName;

	public KeepInventoryCommand(MojmapPlugin<?> plugin, boolean enable) {
		super(plugin);
		this.enable = enable;
		this.effectName = "keep_inventory_" + (enable ? "on" : "off");
		this.displayName = (enable ? "Enable" : "Disable") + " Keep Inventory";
	}

	public static boolean isKeepingInventory(UUID player) {
		return keepingInventory.contains(player);
	}

	public static boolean isKeepingInventory(Entity player) {
		return isKeepingInventory(player.getUUID());
	}

	private void alert(List<ServerPlayer> players) {
		Audience audience = plugin.playerMapper().asAudience(players);
		audience.sendActionBar(enable ? KEEP_INVENTORY_MESSAGE : LOSE_INVENTORY_MESSAGE);
		audience.playSound((enable ? KEEP_INVENTORY_ALERT : LOSE_INVENTORY_ALERT).get(), Sound.Emitter.self());
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder resp = request.buildResponse();

		List<UUID> uuids = new ArrayList<>(players.size());
		for (ServerPlayer player : players)
			uuids.add(player.getUUID());

		if (enable) {
			if (keepingInventory.addAll(uuids)) {
				alert(players);
				return resp.type(ResultType.SUCCESS);
			} else
				return resp.type(ResultType.FAILURE).message("Streamer(s) already have Keep Inventory enabled");
		} else {
			if (keepingInventory.removeAll(uuids)) {
				alert(players);
				return resp.type(ResultType.SUCCESS);
			} else
				return resp.type(ResultType.FAILURE).message("Streamer(s) already have Keep Inventory disabled");
		}
	}

	public static final class Manager {
		@Listener
		public void onDeath(Death event) {
			if (!keepingInventory.contains(event.entity().getUUID())) return;
			event.keepInventory(true);
		}
	}
}
