package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.common.CommandConstants;
import dev.qixils.crowdcontrol.common.CommandConstants.AttributeWeights;
import dev.qixils.crowdcontrol.common.CommandConstants.EnchantmentWeights;
import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.plugin.BukkitCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Source;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class LootboxCommand extends ImmediateCommand {
	private final String effectName = "lootbox";
	private final String displayName = "Open Lootbox";

	public LootboxCommand(BukkitCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		for (Player player : players) {
			Inventory lootbox = Bukkit.createInventory(null, 27, CommandConstants.buildLootboxTitle(request));
			List<Material> items = new ArrayList<>(Arrays.asList(Material.values()));
			Collections.shuffle(items, random);
			Material item = null;
			for (Material i : items) {
				if (i.isItem()) {
					item = i;
					break;
				}
			}
			assert item != null;

			ItemStack itemStack = new ItemStack(item, 1 + random.nextInt(item.getMaxStackSize()));
			ItemMeta itemMeta = itemStack.getItemMeta();
			itemMeta.lore(Collections.singletonList(CommandConstants.buildLootboxLore(request)));
			if (random.nextDouble() >= 0.9D) {
				itemMeta.setUnbreakable(true);
			}

			// big dumb enchantment logic to generate sane items lmfao
			int enchantments = RandomUtil.weightedRandom(EnchantmentWeights.values(), EnchantmentWeights.TOTAL_WEIGHTS).getLevel();
			if (enchantments > 0) {
				List<Enchantment> enchantmentList = new ArrayList<>();
				for (Enchantment enchantment : EnchantmentWrapper.values()) {
					if (enchantment.canEnchantItem(itemStack)) enchantmentList.add(enchantment);
				}
				if (!enchantmentList.isEmpty()) {
					Collections.shuffle(enchantmentList, random);
					Set<Enchantment> addedEnchantments = new HashSet<>();
					int count = 0;
					for (int i = 0; i < enchantmentList.size() && count < enchantments; ++i) {
						Enchantment enchantment = enchantmentList.get(i);
						if (addedEnchantments.stream().noneMatch(x -> x.conflictsWith(enchantment))) {
							++count;
							addedEnchantments.add(enchantment);
							int level = enchantment.getStartLevel() + random.nextInt(enchantment.getMaxLevel() - enchantment.getStartLevel() + 1);
							if (random.nextBoolean()) // bonus OP enchant chance
								level += random.nextInt(4);
							itemStack.addEnchantment(enchantment, level);
						}
					}
				}
			}

			int attributes = RandomUtil.weightedRandom(AttributeWeights.values(), AttributeWeights.TOTAL_WEIGHTS).getLevel();
			if (attributes > 0) {
				List<Attribute> attributeList = Arrays.asList(Attribute.values());
				Collections.shuffle(attributeList, random);
				for (int i = 0; i < attributeList.size() && i < attributes; ++i) {
					Attribute attribute = attributeList.get(i);
					String name = "lootbox_" + attribute.getKey().getKey();
					AttributeModifier attributeModifier = new AttributeModifier(name, (random.nextDouble() * 2) - 1, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
					itemMeta.addAttributeModifier(attribute, attributeModifier);
				}
			}

			itemStack.setItemMeta(itemMeta);
			lootbox.setItem(13, itemStack);
			player.playSound(
					Sound.sound(
							org.bukkit.Sound.BLOCK_NOTE_BLOCK_CHIME,
							Source.PLAYER,
							1f,
							1.2f
					),
					Sound.Emitter.self()
			);
			sync(() -> player.openInventory(lootbox));
		}
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
