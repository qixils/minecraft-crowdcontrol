package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Getter
public class EnchantmentCommand extends ImmediateCommand {
	protected final Enchantment enchantment;
	private final String effectName;
	private final Component displayName;

	public EnchantmentCommand(PaperCrowdControlPlugin plugin, Enchantment enchantment) {
		super(plugin);
		this.enchantment = enchantment;
		this.effectName = "enchant_" + plugin.getTextUtil().translate(enchantment).replace(' ', '_');
		this.displayName = Component.translatable("cc.effect.enchant.name", enchantment.displayName(enchantment.getMaxLevel()).color(null));
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(Response.ResultType.RETRY)
				.message("No items could be enchanted");
		for (Player player : players) {
			int level = enchantment.getMaxLevel();
			PlayerInventory inv = player.getInventory();
			// get the equipped item that supports this enchantment and has the lowest level of it
			Map<EquipmentSlot, Integer> levelMap = new HashMap<>(EquipmentSlot.values().length);
			for (EquipmentSlot slot : EquipmentSlot.values()) {
				ItemStack item = inv.getItem(slot);
				// ensure this item:
				// A) isn't null
				// B) is not empty
				// C) supports the requested enchantment
				// D) would actually benefit from upgrading this enchantment if applicable
				//    (i.e. this prevents Silk Touch from being "upgraded" to level 2)
				// E) is not absolutely maxed out on this enchantment (i.e. level 255)
				if (!item.getType().isEmpty()
						&& enchantment.canEnchantItem(item)
						&& (
						enchantment.getMaxLevel() != enchantment.getStartLevel()
								|| item.getEnchantmentLevel(enchantment) != enchantment.getMaxLevel()
						&& item.getEnchantmentLevel(enchantment) != 255
				)
				) {
					levelMap.put(slot, item.getEnchantmentLevel(enchantment));
				}
			}
			EquipmentSlot slot = levelMap.entrySet().stream()
					.min(Comparator.comparingInt(Entry::getValue))
					.map(Entry::getKey).orElse(null);
			if (slot == null)
				continue;
			// add enchant
			ItemStack item = inv.getItem(slot);
			int curLevel = item.getEnchantmentLevel(enchantment);
			if (curLevel >= level)
				level = curLevel + 1;
			item.addUnsafeEnchantment(enchantment, level);
			result.type(Response.ResultType.SUCCESS);
		}
		return result;
	}
}
