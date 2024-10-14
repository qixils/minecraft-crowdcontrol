package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.common.LimitConfig;
import dev.qixils.crowdcontrol.common.command.QuantityStyle;
import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.registry.RegistryTypes;

import java.util.List;

@Getter
public class TakeItemCommand extends ImmediateCommand {
	private final @NotNull QuantityStyle quantityStyle = QuantityStyle.APPEND_X;
	private final ItemType item;
	private final String effectName;
	private final TranslatableComponent defaultDisplayName;

	public TakeItemCommand(SpongeCrowdControlPlugin plugin, ItemType item) {
		super(plugin);
		this.item = item;
		this.effectName = "take_" + item.key(RegistryTypes.ITEM_TYPE).value();
		this.defaultDisplayName = Component.translatable("cc.effect.take_item.name", item);
	}

	private boolean takeItemFrom(ServerPlayer player, int amount) {
		PlayerInventory inventory = player.inventory();
		// simulate
		int toTake = 0;
		for (Slot invSlot : inventory.slots()) {
			ItemStack stack = invSlot.peek();
			if (stack.isEmpty()) continue;
			if (!stack.type().equals(item)) continue;
			toTake += stack.quantity();
		}
		// do
		if (toTake < amount) return false;
		toTake = amount;
		for (Slot invSlot : inventory.slots()) {
			ItemStack stack = invSlot.peek();
			if (stack.isEmpty()) continue;
			if (!stack.type().equals(item)) continue;
			int take = Math.min(toTake, stack.quantity());
			toTake -= take;
			sync(() -> {
				stack.setQuantity(stack.quantity() - take);
				invSlot.set(stack);
			});
			if (toTake == 0) break;
		}
		return true;
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		int amount = request.getQuantityOrDefault();

		Response.Builder response = request.buildResponse()
				.type(ResultType.RETRY)
				.message("Item could not be found in target inventories");

		LimitConfig config = getPlugin().getLimitConfig();
		int maxVictims = config.getItemLimit(item.key(RegistryTypes.ITEM_TYPE).value());
		int victims = 0;

		// first pass (hosts)
		for (ServerPlayer player : players) {
			if (!config.hostsBypass() && maxVictims > 0 && victims >= maxVictims)
				break;
			if (!isHost(player))
				continue;
			if (takeItemFrom(player, amount))
				victims++;
		}

		// second pass (guests)
		for (ServerPlayer player : players) {
			if (maxVictims > 0 && victims >= maxVictims)
				break;
			if (isHost(player))
				continue;
			if (takeItemFrom(player, amount))
				victims++;
		}

		if (victims > 0)
			response.type(ResultType.SUCCESS).message("SUCCESS");

		return response;
	}
}
