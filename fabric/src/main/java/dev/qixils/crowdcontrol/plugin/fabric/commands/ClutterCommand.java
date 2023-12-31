package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.fabric.utils.ItemUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.CLUTTER_ITEMS;

@Getter
public class ClutterCommand extends ImmediateCommand {
	private final String effectName = "clutter";

	public ClutterCommand(FabricCrowdControlPlugin plugin) {
		super(plugin);
	}

	private static int uniqueSlot(Inventory inventory, Set<Integer> usedSlots) {
		int maxSlot = inventory.getContainerSize();
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

	private static boolean swap(Inventory inventory, int slot1, int slot2) {
		ItemStack item1 = inventory.getItem(slot1);
		ItemStack item2 = inventory.getItem(slot2);
		if (ItemUtil.isSimilar(item1, item2))
			return false;
		inventory.setItem(slot1, item2);
		inventory.setItem(slot2, item1);
		return true;
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		// swaps random items in player's inventory
		boolean success = false;
		for (ServerPlayer player : players) {
			Inventory inventory = player.getInventory();
			Set<Integer> swappedSlots = new HashSet<>(CLUTTER_ITEMS);

			int heldItemSlot = player.getInventory().selected;
			swappedSlots.add(heldItemSlot);
			int heldItemSwap = uniqueSlot(inventory, swappedSlots);
			swappedSlots.add(heldItemSwap);
			success |= swap(inventory, heldItemSlot, heldItemSwap);

			while (swappedSlots.size() < CLUTTER_ITEMS) {
				int newSlot1 = uniqueSlot(inventory, swappedSlots);
				swappedSlots.add(newSlot1);
				int newSlot2 = uniqueSlot(inventory, swappedSlots);
				swappedSlots.add(newSlot2);
				success |= swap(inventory, newSlot1, newSlot2);
			}
		}
		if (success)
			return request.buildResponse().type(Response.ResultType.SUCCESS);
		else
			return request.buildResponse().type(Response.ResultType.RETRY).message("Could not find items to swap");
	}
}
