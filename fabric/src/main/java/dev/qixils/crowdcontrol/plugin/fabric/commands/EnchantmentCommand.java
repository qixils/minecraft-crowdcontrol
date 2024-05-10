package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.Map.Entry;

@Getter
public class EnchantmentCommand extends ImmediateCommand {
	protected final Enchantment enchantment;
	private final String effectName;
	private final Component displayName;

	public EnchantmentCommand(FabricCrowdControlPlugin plugin, Enchantment enchantment) {
		super(plugin);
		this.enchantment = enchantment;
		this.effectName = "enchant_" + csIdOf(Objects.requireNonNull(BuiltInRegistries.ENCHANTMENT.getKey(enchantment), "Enchantment has no registry name"));
		this.displayName = Component.translatable(
				"cc.effect.enchant.name",
				enchantment.getFullname(enchantment.getMaxLevel()).copy().setStyle(Style.EMPTY)
		);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(Response.ResultType.RETRY)
				.message("No items could be enchanted");
		for (ServerPlayer player : players) {
			int level = enchantment.getMaxLevel();
			Inventory inv = player.getInventory();
			// get the equipped item that supports this enchantment and has the lowest level of it
			Map<EquipmentSlot, Integer> levelMap = new HashMap<>(EquipmentSlot.values().length);
			for (EquipmentSlot slot : EquipmentSlot.values()) {
				ItemStack item = getItem(inv, slot);
				// ensure this item:
				// A) isn't null
				// B) is not empty
				// C) supports the requested enchantment
				// D) would actually benefit from upgrading this enchantment if applicable
				//    (i.e. this prevents Silk Touch from being "upgraded" to level 2)
				// E) is not absolutely maxed out on this enchantment (i.e. level 255)
				if (!item.isEmpty()
						&& enchantment.canEnchant(item)
						&& (
						enchantment.getMaxLevel() != enchantment.getMinLevel()
								|| getEnchantmentLevel(item, enchantment) != enchantment.getMaxLevel()
						&& getEnchantmentLevel(item, enchantment) != 255
				)
				) {
					levelMap.put(slot, getEnchantmentLevel(item, enchantment));
				}
			}
			EquipmentSlot slot = levelMap.entrySet().stream()
					.min(Comparator.comparingInt(Entry::getValue))
					.map(Entry::getKey).orElse(null);
			if (slot == null)
				continue;
			// add new enchant
			ItemStack item = getItem(inv, slot);
			int curLevel = getEnchantmentLevel(item, enchantment);
			if (curLevel >= level)
				level = curLevel + 1;
			final int setLevel = level;
			EnchantmentHelper.updateEnchantments(item, mutable -> mutable.set(enchantment, setLevel));
			result.type(Response.ResultType.SUCCESS);
		}
		return result;
	}

	public static ItemStack getItem(Inventory inv, EquipmentSlot slot) {
		if (slot == EquipmentSlot.MAINHAND)
			return inv.getItem(inv.selected);
		if (slot == EquipmentSlot.OFFHAND)
			return inv.offhand.get(0);
		return inv.armor.get(slot.getIndex());
	}

	public static int getEnchantmentLevel(ItemStack item, Enchantment enchantment) {
		return EnchantmentHelper.getItemEnchantmentLevel(enchantment, item);
	}
}
