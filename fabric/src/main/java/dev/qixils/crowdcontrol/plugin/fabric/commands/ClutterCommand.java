package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.CLUTTER_ITEMS;

@Getter
public class ClutterCommand extends ImmediateCommand {
	private final String effectName = "clutter";

	public ClutterCommand(FabricCrowdControlPlugin plugin) {
		super(plugin);
	}

	private static int uniqueSlot(PlayerInventory inventory, Set<Integer> usedSlots) {
		int maxSlot = inventory.size();
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

	private static void swap(PlayerInventory inventory, int slot1, int slot2) {
		ItemStack item1 = inventory.getStack(slot1);
		ItemStack item2 = inventory.getStack(slot2);
		inventory.setStack(slot1, item2);
		inventory.setStack(slot2, item1);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayerEntity> players, @NotNull Request request) {
		// swaps random items in player's inventory
		for (ServerPlayerEntity player : players) {
			PlayerInventory inventory = player.getInventory();
			Set<Integer> swappedSlots = new HashSet<>(CLUTTER_ITEMS);

			int heldItemSlot = player.getInventory().selectedSlot;
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
		}
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
