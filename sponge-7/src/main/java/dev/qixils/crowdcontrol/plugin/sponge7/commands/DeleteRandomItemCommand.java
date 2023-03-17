package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.type.OrderedInventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

@Getter
public class DeleteRandomItemCommand extends ImmediateCommand {
	private final String effectName = "delete_random_item";

	public DeleteRandomItemCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	private static boolean handle(@Nullable ItemStack item) {
		return item != null && !item.isEmpty() && !item.getType().equals(ItemTypes.AIR);
	}

	private static boolean handle(OrderedInventory inv, int index) {
		return handle(inv.poll(new SlotIndex(index)).orElse(null));
	}

	private static boolean handle(OrderedInventory inv, List<Integer> indices) {
		for (int i : indices) {
			if (handle(inv, i)) {
				return true;
			}
		}
		return false;
	}

	private boolean handlePlayer(PlayerInventory inv, List<Integer> indices) {
		if (inv instanceof OrderedInventory)
			return handle((OrderedInventory) inv, indices);

		for (int index : indices) {
			boolean result;
			if (index < 9 * 4)
				result = handle(inv.getMain(), index);
			else if (index < (9 * 4) + 4)
				result = handle(inv.getEquipment(), index - (9 * 4));
			else if (index == (9 * 4) + 4)
				result = handle(inv.getOffhand().poll().orElse(null));
			else {
				plugin.getSLF4JLogger().warn("PlayerInventory index {} is out of bounds", index);
				continue;
			}
			if (result)
				return true;
		}

		return false;
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(Response.ResultType.RETRY)
				.message("No players have items");
		for (Player player : players) {
			if (!(player.getInventory() instanceof PlayerInventory)) {
				plugin.getSLF4JLogger().warn("Player {} has non-PlayerInventory inventory", player.getName());
				continue;
			}
			PlayerInventory inv = (PlayerInventory) player.getInventory();
			List<Integer> indices = IntStream.range(0, inv.capacity()).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
			Collections.shuffle(indices);
			if (handlePlayer(inv, indices))
				result.type(Response.ResultType.SUCCESS).message("SUCCESS");
		}
		return result;
	}
}
