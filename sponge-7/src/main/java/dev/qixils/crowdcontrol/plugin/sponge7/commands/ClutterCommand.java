package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge7.utils.ItemUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.MainPlayerInventory;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.item.inventory.type.GridInventory;

import java.util.*;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.CLUTTER_ITEMS;

@Getter
public class ClutterCommand extends ImmediateCommand {
	private final String effectName = "clutter";

	public ClutterCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	private static SlotPos uniqueSlot(GridInventory inventory, Collection<SlotPos> usedSlots) {
		int rows = inventory.getRows();
		int columns = inventory.getColumns();
		List<SlotPos> allSlots = new ArrayList<>(rows * columns);
		for (int x = 0; x < columns; x++) {
			for (int y = 0; y < rows; y++) {
				allSlots.add(SlotPos.of(x, y));
			}
		}
		Collections.shuffle(allSlots, random);

		for (SlotPos slot : allSlots) {
			if (!usedSlots.contains(slot))
				return slot;
		}
		throw new IllegalArgumentException("All slots have been used");
	}

	private static Slot unwrap(@Nullable Slot slot, SlotPos pos) {
		if (slot == null)
			throw new IndexOutOfBoundsException("Slot " + pos.getX() + "," + pos.getY() + " is out of bounds");
		return slot;
	}

	private static Slot unwrap(GridInventory inventory, SlotPos pos) {
		try {
			return unwrap(inventory.getSlot(pos).orElse(null), pos);
		} catch (IndexOutOfBoundsException e) {
			// workaround for Sponge#3584
			int offset = 9 * (pos.getY() - 1);
			SlotPos hackPos = SlotPos.of(pos.getX() + offset, 1);
			return unwrap(inventory.getSlot(hackPos).orElse(null), pos);
		}
	}

	private static boolean swap(MainPlayerInventory inv, SlotPos slotPos1, SlotPos slotPos2) {
		Slot slot1 = unwrap(inv, slotPos1);
		Slot slot2 = unwrap(inv, slotPos2);

		ItemStack slot1item = slot1.poll().orElseGet(ItemStack::empty);
		ItemStack slot2item = slot2.poll().orElseGet(ItemStack::empty);

		if (ItemUtil.isSimilar(slot1item, slot2item))
			return false;

		slot2.offer(slot1item);
		slot1.offer(slot2item);

		return true;
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		// swaps random items in player's inventory
		boolean success = false;
		for (Player player : players) {
			if (!(player.getInventory() instanceof PlayerInventory)) {
				plugin.getSLF4JLogger().warn("Player " + player.getName() + "'s inventory "
						+ player.getInventory().getClass().getSimpleName()
						+ " is not an instance of PlayerInventory");
				continue;
			}
			MainPlayerInventory inventory = ((PlayerInventory) player.getInventory()).getMain();
			Set<SlotPos> swappedSlots = new HashSet<>(CLUTTER_ITEMS);

			SlotPos heldItemSlot = SlotPos.of(inventory.getHotbar().getSelectedSlotIndex(), 0);
			swappedSlots.add(heldItemSlot);
			SlotPos swapItemWith = uniqueSlot(inventory, swappedSlots);
			swappedSlots.add(swapItemWith);
			success |= swap(inventory, heldItemSlot, swapItemWith);

			while (swappedSlots.size() < CLUTTER_ITEMS) {
				SlotPos newSlot1 = uniqueSlot(inventory, swappedSlots);
				swappedSlots.add(newSlot1);
				SlotPos newSlot2 = uniqueSlot(inventory, swappedSlots);
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
