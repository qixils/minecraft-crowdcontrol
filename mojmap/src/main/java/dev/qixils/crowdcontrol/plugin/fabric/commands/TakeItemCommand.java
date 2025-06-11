package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.LimitConfig;
import dev.qixils.crowdcontrol.common.command.QuantityStyle;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.utils.InventoryUtil;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

@Getter
public class TakeItemCommand extends ModdedCommand implements ItemCommand {
	private final @NotNull QuantityStyle quantityStyle = QuantityStyle.APPEND_X;
	private final Item item;
	private final String effectName;
	private final TranslatableComponent defaultDisplayName;

	public TakeItemCommand(ModdedCrowdControlPlugin plugin, Item item) {
		super(plugin);
		this.item = item;
		this.effectName = "take_" + BuiltInRegistries.ITEM.getKey(item).getPath();
		this.defaultDisplayName = Component.translatable("cc.effect.take_item.name", plugin.toAdventure(item.getName(new ItemStack(item))));
	}

	private boolean takeItemFrom(Player player, int amount) {
		Inventory inventory = player.getInventory();
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
			itemStack.shrink(take);
			toTake -= take;
			if (toTake == 0) break;
		}
		return true;
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			List<ServerPlayer> players = playerSupplier.get();

			LimitConfig config = getPlugin().getLimitConfig();
			int playerLimit = config.getItemLimit(BuiltInRegistries.ITEM.getKey(item).getPath());

			int amount = request.getQuantity();

			return executeLimit(request, players, playerLimit, player -> {
				boolean success = false;
				try {
					success = takeItemFrom(player, amount);
				} catch (Exception ignored) {
				}
				return success
					? new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS)
					: new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Item could not be found in target inventories");
			});
		}));
	}
}
