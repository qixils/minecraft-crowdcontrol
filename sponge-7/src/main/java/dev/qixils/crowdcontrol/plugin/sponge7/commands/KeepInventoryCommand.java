package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import com.flowpowered.math.vector.Vector3d;
import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DestructEntityEvent;

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
	private static boolean globalKeepInventory = false;

	public KeepInventoryCommand(SpongeCrowdControlPlugin plugin, boolean enable) {
		super(plugin);
		this.enable = enable;
		this.effectName = "keep_inventory_" + (enable ? "on" : "off");
		this.displayName = (enable ? "Enable" : "Disable") + " Keep Inventory";
	}

	public static boolean isKeepingInventory(UUID player) {
		return globalKeepInventory || keepingInventory.contains(player);
	}

	public static boolean isKeepingInventory(Entity player) {
		return isKeepingInventory(player.getUniqueId());
	}

	private void alert(List<Player> players) {
		Component actionBar = enable ? KEEP_INVENTORY_MESSAGE : LOSE_INVENTORY_MESSAGE;
		Sound sound = (enable ? KEEP_INVENTORY_ALERT : LOSE_INVENTORY_ALERT).get();
		for (Player player : players) {
			Audience audience = plugin.asAudience(player);
			audience.sendActionBar(actionBar);
			Vector3d pos = player.getPosition();
			audience.playSound(sound, pos.getX(), pos.getY(), pos.getZ());
		}
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder resp = request.buildResponse();

		if (isGlobalCommandUsable(players, request)) {
			if (globalKeepInventory == enable) {
				return resp.type(ResultType.FAILURE).message("Keep Inventory is already " + (enable ? "enabled" : "disabled"));
			}
			globalKeepInventory = enable;
			alert(players);
			return resp.type(ResultType.SUCCESS);
		}

		List<UUID> uuids = new ArrayList<>(players.size());
		for (Player player : players)
			uuids.add(player.getUniqueId());

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
		public void onDeath(DestructEntityEvent.Death event) {
			if (!isKeepingInventory(event.getTargetEntity()))
				return;
			event.setKeepInventory(true);
		}
	}
}
