package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.RegionalCommandSync;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

@Getter
public class RemoveEnchantsCommand extends RegionalCommandSync {
	private final String effectName = "remove_enchants";
	private final EquipmentSlot[] slots = new EquipmentSlot[] {
		EquipmentSlot.HAND,
		EquipmentSlot.OFF_HAND,
		EquipmentSlot.CHEST,
		EquipmentSlot.LEGS,
		EquipmentSlot.FEET,
		EquipmentSlot.HEAD
	};

	public RemoveEnchantsCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	protected Response.@NotNull Builder buildFailure(@NotNull Request request) {
		return request.buildResponse()
			.type(Response.ResultType.RETRY)
			.message("Target was not holding an enchanted item");
	}

	@Override
	protected boolean executeRegionallySync(@NotNull Player player, @NotNull Request request) {
		PlayerInventory inv = player.getInventory();
		for (EquipmentSlot slot : slots) {
			ItemStack item = inv.getItem(slot);
			if (tryRemoveEnchants(item))
				return true;
		}
		return false;
	}

	private boolean tryRemoveEnchants(@Nullable ItemStack item) {
		if (item == null)
			return false;
		if (item.getType().isEmpty())
			return false;
		if (!item.hasItemMeta())
			return false;
		ItemMeta meta = item.getItemMeta();
		if (!meta.hasEnchants())
			return false;
		// immutable copy of enchants
		Set<Enchantment> enchants = meta.getEnchants().keySet();
		if (enchants.isEmpty())
			return false;

		enchants.forEach(item::removeEnchantment);
		return true;
	}
}
