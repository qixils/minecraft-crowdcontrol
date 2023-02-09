package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.PrimaryPlayerInventory;
import org.spongepowered.api.item.inventory.type.GridInventory;
import org.spongepowered.math.vector.Vector2i;

import java.util.*;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.CLUTTER_ITEMS;

@Getter
public class ClutterCommand extends ImmediateCommand {
	private final String effectName = "clutter";

	public ClutterCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	private static Vector2i uniqueSlot(GridInventory inventory, Collection<Vector2i> usedSlots) {
		int rows = inventory.rows();
		int columns = inventory.columns();
		List<Vector2i> allSlots = new ArrayList<>(rows * columns);
		for (int x = 0; x < columns; x++) {
			for (int y = 0; y < rows; y++) {
				allSlots.add(Vector2i.from(x, y));
			}
		}
		Collections.shuffle(allSlots, random);

		for (Vector2i slot : allSlots) {
			if (!usedSlots.contains(slot))
				return slot;
		}
		throw new IllegalArgumentException("All slots have been used");
	}

	private static Slot unwrap(@Nullable Slot slot, Vector2i pos) {
		if (slot == null)
			throw new IndexOutOfBoundsException("Slot " + pos.x() + "," + pos.y() + " is out of bounds");
		return slot;
	}

	private static Slot unwrap(GridInventory inventory, Vector2i pos) {
		return unwrap(inventory.slot(pos).orElse(null), pos);
	}

	private static void swap(GridInventory inv, Vector2i slotPos1, Vector2i slotPos2) {
		Slot slot1 = unwrap(inv, slotPos1);
		Slot slot2 = unwrap(inv, slotPos2);

		ItemStack slot1item = slot1.poll().polledItem().createStack();
		ItemStack slot2item = slot2.poll().polledItem().createStack();

		// this is throwing warnings in the console which I cannot silence >.>
		slot2.offer(slot1item);
		slot1.offer(slot2item);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		// swaps random items in player's inventory
		sync(() -> {
			try (StackFrame frame = plugin.getGame().server().causeStackManager().pushCauseFrame()) {
				frame.pushCause(plugin.getPluginContainer());
				for (ServerPlayer player : players) {
					PrimaryPlayerInventory inventory = player.inventory().primary();
					GridInventory gridInventory = inventory.asGrid();
					Set<Vector2i> swappedSlots = new HashSet<>(CLUTTER_ITEMS);

					Vector2i heldItemSlot = Vector2i.from(inventory.hotbar().selectedSlotIndex(), 3);
					swappedSlots.add(heldItemSlot);
					Vector2i swapItemWith = uniqueSlot(gridInventory, swappedSlots);
					swappedSlots.add(swapItemWith);
					swap(gridInventory, heldItemSlot, swapItemWith);

					while (swappedSlots.size() < CLUTTER_ITEMS) {
						Vector2i newSlot1 = uniqueSlot(gridInventory, swappedSlots);
						swappedSlots.add(newSlot1);
						Vector2i newSlot2 = uniqueSlot(gridInventory, swappedSlots);
						swappedSlots.add(newSlot2);
						swap(gridInventory, newSlot1, newSlot2);
					}
				}
				frame.popCause();
			}
		});
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
