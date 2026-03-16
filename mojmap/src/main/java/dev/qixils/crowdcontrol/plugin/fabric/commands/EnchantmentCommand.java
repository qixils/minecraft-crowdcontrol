package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.CCName;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.minecraft.core.Holder;
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
import java.util.function.Supplier;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.csIdOf;

@Getter
public class EnchantmentCommand extends ModdedCommand {
	protected final Holder<Enchantment> enchantmentHolder;
	protected final Enchantment enchantment;
	private final String effectName;
	private final Component displayName;
	private final CCName extensionName;
	private final String image = "enchant_sharpness";
	private final int price = 50;
	private final byte priority = 0;
	private final List<String> categories = Collections.singletonList("Enchantments");

	public EnchantmentCommand(ModdedCrowdControlPlugin plugin, Holder<Enchantment> enchantment) {
		super(plugin);
		this.enchantmentHolder = enchantment;
		this.enchantment = enchantmentHolder.value();
		this.effectName = "enchant_" + csIdOf(enchantment.unwrapKey().orElseThrow());
		TranslatableComponent _displayName = Component.translatable(
				"cc.effect.enchant.name",
				plugin.toAdventure(Enchantment.getFullname(enchantment, enchantment.value().getMaxLevel()).copy().setStyle(Style.EMPTY))
		);
		this.displayName = _displayName;
		this.extensionName = new CCName(plugin.getTextUtil().asPlain(_displayName.key("cc.effect.enchant.extension")));
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			boolean success = false;
			for (ServerPlayer player : playerSupplier.get()) {
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
							|| getEnchantmentLevel(item, enchantmentHolder) != enchantment.getMaxLevel()
							&& getEnchantmentLevel(item, enchantmentHolder) != 255
					)
					) {
						levelMap.put(slot, getEnchantmentLevel(item, enchantmentHolder));
					}
				}
				EquipmentSlot slot = levelMap.entrySet().stream()
					.min(Comparator.comparingInt(Entry::getValue))
					.map(Entry::getKey).orElse(null);
				if (slot == null)
					continue;
				// add new enchant
				ItemStack item = getItem(inv, slot);
				int curLevel = getEnchantmentLevel(item, enchantmentHolder);
				if (curLevel >= level)
					level = curLevel + 1;
				final int setLevel = level;
				EnchantmentHelper.updateEnchantments(item, mutable -> mutable.set(enchantmentHolder, setLevel));
				success = true;
			}
			return success
				? new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS)
				: new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "No items could be enchanted");
		}));
	}

	public static ItemStack getItem(Inventory inv, EquipmentSlot slot) {
		if (slot == EquipmentSlot.MAINHAND)
			return inv.getSelectedItem();
		return inv.getItem(slot.getIndex(Inventory.INVENTORY_SIZE));
	}

	public static int getEnchantmentLevel(ItemStack item, Holder<Enchantment> enchantment) {
		return EnchantmentHelper.getItemEnchantmentLevel(enchantment, item);
	}
}
