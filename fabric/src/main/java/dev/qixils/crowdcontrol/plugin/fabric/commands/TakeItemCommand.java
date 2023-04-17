package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.LimitConfig;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.fabric.utils.InventoryUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TakeItemCommand extends ImmediateCommand implements ItemCommand {
	private final Item item;
	private final String effectName;
	private final TranslatableComponent defaultDisplayName;

	public TakeItemCommand(FabricCrowdControlPlugin plugin, Item item) {
		super(plugin);
		this.item = item;
		this.effectName = "take_" + Registries.ITEM.getId(item).getPath();
		this.defaultDisplayName = Component.translatable("cc.effect.take_item.name", item.getName(new ItemStack(item)));
	}

	@Override
	public @NotNull Component getProcessedDisplayName(@NotNull Request request) {
		if (request.getParameters() == null)
			return getDefaultDisplayName();
		int amount = (int) (double) request.getParameters()[0];
		TranslatableComponent displayName = getDefaultDisplayName().key("cc.effect.take_item_x.name");
		List<Component> args = new ArrayList<>(displayName.args());
		args.add(Component.text(amount));
		return displayName.args(args);
	}

	private boolean takeItemFrom(PlayerEntity player, int amount) {
		PlayerInventory inventory = player.getInventory();
		// simulate
		int toTake = 0;
		for (ItemStack itemStack : InventoryUtil.viewAllItems(inventory)) {
			if (itemStack.isEmpty()) continue;
			if (itemStack.getItem() != this.item) continue;
			toTake += itemStack.getCount();
			if (toTake >= amount) break;
		}
		// do
		if (toTake < amount) return false;
		toTake = amount;
		for (ItemStack itemStack : InventoryUtil.viewAllItems(inventory)) {
			if (itemStack.isEmpty()) continue;
			if (itemStack.getItem() != this.item) continue;
			int take = Math.min(toTake, itemStack.getCount());
			itemStack.decrement(take);
			toTake -= take;
			if (toTake == 0) break;
		}
		return true;
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayerEntity> players, @NotNull Request request) {
		int amount = request.getParameters() == null ? 1 : (int) (double) request.getParameters()[0];
		Response.Builder response = request.buildResponse()
				.type(ResultType.RETRY)
				.message("Item could not be found in target inventories");

		LimitConfig config = getPlugin().getLimitConfig();
		int maxVictims = config.getItemLimit(Registries.ITEM.getId(item).getPath());
		int victims = 0;

		// first pass (hosts)
		for (ServerPlayerEntity player : players) {
			if (!config.hostsBypass() && maxVictims > 0 && victims >= maxVictims)
				break;
			if (!isHost(player))
				continue;
			if (takeItemFrom(player, amount))
				victims++;
		}

		// second pass (guests)
		for (ServerPlayerEntity player : players) {
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
