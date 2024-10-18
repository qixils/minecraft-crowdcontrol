package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.command.QuantityStyle;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.RegionalCommandSync;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

@Getter
public class TakeItemCommand extends RegionalCommandSync implements ItemCommand {
	private final @NotNull QuantityStyle quantityStyle = QuantityStyle.APPEND_X;
	private final Material item;
	private final String effectName;
	private final TranslatableComponent defaultDisplayName;

	public TakeItemCommand(PaperCrowdControlPlugin plugin, Material item) {
		super(plugin);
		this.item = item;
		this.effectName = "take_" + item.key().value();
		this.defaultDisplayName = Component.translatable("cc.effect.take_item.name", Component.translatable(new ItemStack(item)));
	}

	@Override
	protected @NotNull CCEffectResponse buildFailure(@NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Item could not be found in target inventories");
	}

	@Override
	protected boolean executeRegionallySync(@NotNull Player player, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		int amount = request.getQuantity();
		PlayerInventory inventory = player.getInventory();
		// simulate
		int toTake = 0;
		for (ItemStack itemStack : inventory) {
			if (itemStack == null) continue;
			if (itemStack.getType() != this.item) continue;
			toTake += itemStack.getAmount();
			if (toTake >= amount) break;
		}
		// do
		if (toTake < amount) return false;
		toTake = amount;
		for (int i = 0; i < inventory.getSize(); i++) {
			ItemStack itemStack = inventory.getItem(i);
			if (itemStack == null) continue;
			if (itemStack.getType() != this.item) continue;
			int take = Math.min(itemStack.getAmount(), toTake);
			itemStack.setAmount(itemStack.getAmount() - take);
			toTake -= take;
			if (toTake == 0) break;
		}
		return true;
	}
}
