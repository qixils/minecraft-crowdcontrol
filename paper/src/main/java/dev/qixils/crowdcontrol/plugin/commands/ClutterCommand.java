package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.BukkitCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static dev.qixils.crowdcontrol.common.CommandConstants.CLUTTER_ITEMS;

@Getter
public class ClutterCommand extends ImmediateCommand {
	private final String effectName = "clutter";
	private final String displayName = "Clutter Inventory";

	public ClutterCommand(BukkitCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		// swaps random items in player's inventory
		// TODO: improve implementation (similar to Sponge)
		for (Player player : players) {
			PlayerInventory inventory = player.getInventory();
			int maxSlots = inventory.getSize();
			Set<Integer> slots = new HashSet<>(CLUTTER_ITEMS * 2);
			slots.add(inventory.getHeldItemSlot());
			while (slots.size() < CLUTTER_ITEMS)
				slots.add(random.nextInt(maxSlots));
			for (int slot : slots) {
				ItemStack hand = inventory.getItem(slot);
				// lazy workaround to get a unique slot lmfao
				int destSlot = slot;
				while (destSlot == slot) {
					destSlot = random.nextInt(maxSlots);
				}
				ItemStack swap = inventory.getItem(destSlot);
				inventory.setItem(slot, swap);
				inventory.setItem(destSlot, hand);
			}
			player.updateInventory();
		}
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
