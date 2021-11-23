package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.utils.TextBuilder;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
public class KeepInventoryCommand extends ImmediateCommand {
	private final boolean enable;
	private final String effectName;
	private final String displayName;
	private static final Set<UUID> keepingInventory = Collections.synchronizedSet(new HashSet<>(1));

	public static boolean isKeepingInventory(UUID player) {
		return keepingInventory.contains(player);
	}

	public static boolean isKeepingInventory(Entity player) {
		return keepingInventory.contains(player.getUniqueId());
	}

	public KeepInventoryCommand(CrowdControlPlugin plugin, boolean enable) {
		super(plugin);
		this.enable = enable;
		this.effectName = "keep_inventory_" + (enable ? "on" : "off");
		this.displayName = (enable ? "Enable" : "Disable") + " Keep Inventory";
	}

	@Override
	protected @NotNull Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder resp = request.buildResponse();

		List<UUID> uuids = new ArrayList<>(players.size());
		for (Player player : players)
			uuids.add(player.getUniqueId());

		if (enable) {
			if (keepingInventory.addAll(uuids))
				return resp.type(ResultType.SUCCESS);
			else
				return resp.type(ResultType.FAILURE).message("Streamer(s) already have Keep Inventory enabled");
		} else {
			if (keepingInventory.removeAll(uuids))
				return resp.type(ResultType.SUCCESS);
			else
				return resp.type(ResultType.FAILURE).message("Streamer(s) already have Keep Inventory disabled");
		}
	}

	public static final class Manager implements Listener {
		@EventHandler
		public void onDeath(PlayerDeathEvent event) {
			if (!keepingInventory.contains(event.getEntity().getUniqueId())) return;
			event.setKeepInventory(true);
			event.getDrops().clear();
			event.setKeepLevel(true);
			event.setDroppedExp(0);
		}

		private static final Component KEEP_COMPONENT = new TextBuilder(
				"Your inventory will be kept on death",
				NamedTextColor.GREEN
		).build();

		private static final Component LOSE_COMPONENT = new TextBuilder(NamedTextColor.RED)
				.next("Your inventory will &lnot")
				.next(" be kept on death").build();

		// run asynchronously at 10tps (setup in RegisterCommands)
		public static void renderActionBars() {
			for (Player player : Bukkit.getOnlinePlayers()) {
				player.sendActionBar(
						keepingInventory.contains(player.getUniqueId())
								? KEEP_COMPONENT
								: LOSE_COMPONENT
				);
			}
		}
	}
}
