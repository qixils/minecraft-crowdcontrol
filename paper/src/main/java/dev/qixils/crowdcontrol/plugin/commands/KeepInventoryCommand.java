package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.common.util.TextBuilder;
import dev.qixils.crowdcontrol.plugin.BukkitCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Source;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
public class KeepInventoryCommand extends ImmediateCommand {
	private static final Component ENABLED_MESSAGE = new TextBuilder(
			"Your inventory will be kept on death",
			NamedTextColor.GREEN
	).build();

	private static final Component DISABLED_MESSAGE = new TextBuilder(NamedTextColor.RED)
			.next("Your inventory will &lnot")
			.next(" be kept on death").build();

	private static final Sound ENABLED_ALERT = Sound.sound(
			Key.key(Key.MINECRAFT_NAMESPACE, "block.beacon.activate"),
			Source.MASTER,
			1f,
			1f
	);

	private static final Sound DISABLED_ALERT = Sound.sound(
			Key.key(Key.MINECRAFT_NAMESPACE, "block.beacon.deactivate"),
			Source.MASTER,
			1f,
			1f
	);

	private static final Set<UUID> keepingInventory = Collections.synchronizedSet(new HashSet<>(1));

	private final boolean enable;
	private final String effectName;
	private final String displayName;

	public KeepInventoryCommand(BukkitCrowdControlPlugin plugin, boolean enable) {
		super(plugin);
		this.enable = enable;
		this.effectName = "keep_inventory_" + (enable ? "on" : "off");
		this.displayName = (enable ? "Enable" : "Disable") + " Keep Inventory";
	}

	public static boolean isKeepingInventory(UUID player) {
		return keepingInventory.contains(player);
	}

	public static boolean isKeepingInventory(Entity player) {
		return keepingInventory.contains(player.getUniqueId());
	}

	private void alert(Collection<? extends Audience> players) {
		Audience audience = Audience.audience(players);
		audience.sendActionBar(enable ? ENABLED_MESSAGE : DISABLED_MESSAGE);
		audience.playSound(enable ? ENABLED_ALERT : DISABLED_ALERT);
	}

	@Override
	@NotNull
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder resp = request.buildResponse();

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

	public static final class Manager implements Listener {
		@EventHandler
		public void onDeath(PlayerDeathEvent event) {
			if (!keepingInventory.contains(event.getEntity().getUniqueId())) return;
			event.setKeepInventory(true);
			event.getDrops().clear();
			event.setKeepLevel(true);
			event.setDroppedExp(0);
		}
	}
}
