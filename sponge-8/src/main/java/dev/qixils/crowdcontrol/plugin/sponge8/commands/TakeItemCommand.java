package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.registry.RegistryTypes;

import java.util.List;

@Getter
public class TakeItemCommand extends ImmediateCommand {
	private final ItemType item;
	private final String effectName;
	private final String displayName;

	public TakeItemCommand(SpongeCrowdControlPlugin plugin, ItemType item) {
		super(plugin);
		this.item = item;
		this.effectName = "take_" + item.key(RegistryTypes.ITEM_TYPE).value();
		this.displayName = "Take " + plugin.getTextUtil().asPlain(item);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder response = request.buildResponse()
				.type(ResultType.RETRY)
				.message("Item could not be found in target inventories");
		for (ServerPlayer player : players) {
			PlayerInventory inventory = player.inventory();
			for (Slot invSlot : inventory.slots()) {
				ItemStack stack = invSlot.peek();
				if (stack.isEmpty())
					continue;
				if (!stack.type().equals(item))
					continue;

				response.type(ResultType.SUCCESS).message("SUCCESS");
				sync(() -> {
					stack.setQuantity(stack.quantity() - 1);
					invSlot.set(stack);
				});
				break;
			}

			if (ResultType.SUCCESS == response.type() && item.equals(ItemTypes.END_PORTAL_FRAME.get()))
				break;
		}
		return response;
	}
}
