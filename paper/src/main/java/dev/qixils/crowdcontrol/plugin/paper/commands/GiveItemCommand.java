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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;

@Getter
public class GiveItemCommand extends RegionalCommandSync implements ItemCommand {
	private final @NotNull QuantityStyle quantityStyle = QuantityStyle.APPEND_X;
	private final Material item;
	private final String effectName;
	private final TranslatableComponent defaultDisplayName;

	public GiveItemCommand(PaperCrowdControlPlugin plugin, Material item) {
		super(plugin);
		this.item = item;
		this.effectName = "give_" + item.key().value();
		this.defaultDisplayName = Component.translatable("cc.effect.give_item.name", Component.translatable(new ItemStack(item)));
	}

	@Blocking
	public static boolean giveItemTo(Entity player, ItemStack itemStack) {
		Location location = player.getLocation();
		Item item = (Item) player.getWorld().spawnEntity(location, EntityType.ITEM);
		item.setItemStack(itemStack);
		item.setOwner(player.getUniqueId());
		item.setThrower(player.getUniqueId());
		item.setCanMobPickup(false);
		item.setCanPlayerPickup(true);
		item.setPickupDelay(0);
		return true;
	}

	@Override
	protected @NotNull CCEffectResponse buildFailure(@NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Failed to spawn item");
	}

	@Override
	protected int getPlayerLimit() {
		return getPlugin().getLimitConfig().getItemLimit(item.getKey().getKey());
	}

	@Override
	protected boolean executeRegionallySync(@NotNull Player player, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		int amount = request.getQuantity();
		ItemStack itemStack = new ItemStack(item, amount);
		return giveItemTo(player, itemStack);
	}
}
