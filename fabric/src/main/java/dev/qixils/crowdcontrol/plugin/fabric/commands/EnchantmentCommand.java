package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
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
		this.effectName = "enchant_" + Registry.ENCHANTMENT.getKey(enchantment).getPath();
		this.displayName = Component.translatable(
				"cc.effect.enchant.name",
				enchantment.getFullname(enchantment.getMaxLevel()).copy().withStyle(Style.EMPTY)
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
				if (!item.isEmpty()
						&& enchantment.canEnchant(item)
						&& (
						enchantment.getMaxLevel() != enchantment.getMinLevel()
								|| getEnchantmentLevel(item, enchantment) != enchantment.getMaxLevel()
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
			// remove existing enchant
			ResourceLocation tag = Registry.ENCHANTMENT.getKey(enchantment);
			ItemStack item = getItem(inv, slot);
			int curLevel = getEnchantmentLevel(item, enchantment);
			item.getEnchantmentTags().removeIf(ench ->
					Objects.equals(tag, EnchantmentHelper.getEnchantmentId((CompoundTag) ench)));
			// add new enchant
			if (curLevel >= level)
				level = curLevel + 1;
			item.enchant(enchantment, level);
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
