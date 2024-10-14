package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.RegionalCommandSync;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.ITEM_DAMAGE_PERCENTAGE;

@Getter
public class ItemDamageCommand extends RegionalCommandSync {
	private final BiFunction<Integer, Material, Integer> handleItem;
	private final String effectName;
	private final EquipmentSlot[] slots = new EquipmentSlot[] {
		EquipmentSlot.HAND,
		EquipmentSlot.OFF_HAND,
		EquipmentSlot.CHEST,
		EquipmentSlot.LEGS,
		EquipmentSlot.FEET,
		EquipmentSlot.HEAD
	};

	public ItemDamageCommand(PaperCrowdControlPlugin plugin, boolean repair) {
		super(plugin);
		handleItem = repair
				? (damage, type) -> 0
				: (damage, type) -> damage + (type.getMaxDurability() / ITEM_DAMAGE_PERCENTAGE);
		effectName = (repair ? "repair" : "damage") + "_item";
	}

	@Override
	protected Response.@NotNull Builder buildFailure(@NotNull Request request) {
		return request.buildResponse()
			.type(Response.ResultType.RETRY)
			.message("Player(s) not holding a damaged item");
	}

	@Override
	protected boolean executeRegionallySync(@NotNull Player player, @NotNull Request request) {
		// create list of random equipment slots
		List<EquipmentSlot> slots = new ArrayList<>(Arrays.asList(this.slots));
		Collections.shuffle(slots);

		PlayerInventory inv = player.getInventory();
		for (EquipmentSlot slot : slots) {
			ItemStack item = inv.getItem(slot);
			if (item.getType().isEmpty())
				continue;
			if (item.getAmount() < 1)
				continue;
			if (item.getType().getMaxDurability() <= 1)
				continue;
			ItemMeta meta = item.getItemMeta();
			if (meta.isUnbreakable())
				continue;
			if (!(meta instanceof Damageable damageable))
				continue;
			Material type = item.getType();
			int curDamage = damageable.getDamage();
			int newDamage = handleItem.apply(damageable.getDamage(), type);
			if (CommandConstants.canApplyDamage(curDamage, newDamage, type.getMaxDurability())) {
				damageable.setDamage(newDamage);
				item.setItemMeta(damageable);
				return true;
			}
		}

		return false;
	}
}
