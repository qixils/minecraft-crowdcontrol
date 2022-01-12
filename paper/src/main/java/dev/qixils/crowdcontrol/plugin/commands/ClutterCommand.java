package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.BukkitCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
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

	private static int uniqueSlot(PlayerInventory inventory, Set<Integer> usedSlots) {
		int maxSlot = inventory.getSize();
		List<Integer> allSlots = new ArrayList<>(maxSlot);
		for (int i = 0; i < maxSlot; i++)
			allSlots.add(i);
		Collections.shuffle(allSlots, random);
		for (int slot : allSlots) {
			if (!usedSlots.contains(slot))
				return slot;
		}
		throw new IllegalArgumentException("All slots have been used");
	}

	private static void swap(Inventory inventory, int slot1, int slot2) {
		ItemStack item1 = inventory.getItem(slot1);
		ItemStack item2 = inventory.getItem(slot2);
		inventory.setItem(slot1, item2);
		inventory.setItem(slot2, item1);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		// swaps random items in player's inventory
		for (Player player : players) {
			PlayerInventory inventory = player.getInventory();
			Set<Integer> swappedSlots = new HashSet<>(CLUTTER_ITEMS);

			int heldItemSlot = inventory.getHeldItemSlot();
			swappedSlots.add(heldItemSlot);
			int heldItemSwap = uniqueSlot(inventory, swappedSlots);
			swappedSlots.add(heldItemSwap);
			swap(inventory, heldItemSlot, heldItemSwap);

			while (swappedSlots.size() < CLUTTER_ITEMS) {
				int newSlot1 = uniqueSlot(inventory, swappedSlots);
				swappedSlots.add(newSlot1);
				int newSlot2 = uniqueSlot(inventory, swappedSlots);
				swappedSlots.add(newSlot2);
				swap(inventory, newSlot1, newSlot2);
			}

			player.updateInventory();
		}
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
