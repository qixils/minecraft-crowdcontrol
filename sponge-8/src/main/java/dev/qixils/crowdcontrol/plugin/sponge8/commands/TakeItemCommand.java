package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.type.CarriedInventory;

import java.util.List;
import java.util.Optional;

@Getter
public class TakeItemCommand extends ImmediateCommand {
	private final ItemType item;
	private final String effectName;
	private final String displayName;

	public TakeItemCommand(SpongeCrowdControlPlugin plugin, ItemType item) {
		super(plugin);
		this.item = item;
		this.effectName = "take_" + SpongeTextUtil.valueOf(item);
		this.displayName = "Take " + item.getTranslation().get();
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder response = request.buildResponse()
				.type(ResultType.RETRY)
				.message("Item could not be found in target inventories");
		for (Player player : players) {
			CarriedInventory<? extends Carrier> inventory = player.getInventory();
			for (Inventory invSlot : inventory.slots()) {
				Optional<ItemStack> optionalStack = invSlot.peek();
				if (!optionalStack.isPresent())
					continue;

				ItemStack stack = optionalStack.get();
				if (!stack.getType().equals(item))
					continue;

				response.type(ResultType.SUCCESS).message("SUCCESS");
				sync(() -> {
					stack.setQuantity(stack.getQuantity() - 1);
					invSlot.set(stack);
				});
				break;
			}

			if (ResultType.SUCCESS == response.type() && item.equals(ItemTypes.END_PORTAL_FRAME))
				break;
		}
		return response;
	}
}
