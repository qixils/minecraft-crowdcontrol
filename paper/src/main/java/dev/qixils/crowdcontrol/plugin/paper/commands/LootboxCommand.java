package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.CommandConstants;
import dev.qixils.crowdcontrol.common.CommandConstants.AttributeWeights;
import dev.qixils.crowdcontrol.common.CommandConstants.EnchantmentWeights;
import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.common.util.sound.Sounds;
import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.kyori.adventure.sound.Sound;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class LootboxCommand extends ImmediateCommand {
	private static final List<Material> ITEMS = Arrays.stream(Material.values()).filter(Material::isItem).toList();
	private static final Set<Material> GOOD_ITEMS = ITEMS.stream().filter(material ->
			material.getMaxDurability() > 1
					|| material == Material.GOLDEN_APPLE
					|| material == Material.ENCHANTED_GOLDEN_APPLE
					|| material == Material.DIAMOND_BLOCK
					|| material == Material.NETHERITE_BLOCK
					|| material == Material.IRON_BLOCK
					|| material == Material.GOLD_BLOCK
	).collect(Collectors.toUnmodifiableSet());
	private static final List<Attribute> ATTRIBUTES = Arrays.asList(
			Attribute.GENERIC_MAX_HEALTH,
			Attribute.GENERIC_KNOCKBACK_RESISTANCE,
			Attribute.GENERIC_MOVEMENT_SPEED,
			Attribute.GENERIC_ATTACK_DAMAGE,
			Attribute.GENERIC_ARMOR,
			Attribute.GENERIC_ARMOR_TOUGHNESS,
			Attribute.GENERIC_ATTACK_KNOCKBACK,
			Attribute.GENERIC_ATTACK_SPEED
	);
	private final String effectName = "lootbox";
	private final String displayName = "Open Lootbox";

	public LootboxCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	private static boolean isGoodItem(@Nullable Material item) {
		return item != null && GOOD_ITEMS.contains(item);
	}

	/**
	 * Creates a random item that is influenced by the supplied luck value.
	 * The item may contain enchantments and modifiers if applicable.
	 *
	 * @param luck zero-indexed level of luck
	 * @return new random item
	 */
	public static ItemStack createRandomItem(int luck) {
		// determine the item used in the stack
		// "good" items have a higher likelihood of being picked with positive luck
		List<Material> items = new ArrayList<>(ITEMS);
		Collections.shuffle(items, random);
		Material item = null;
		for (int i = 0; i <= luck * 4; i++) {
			Material oldItem = item;
			item = items.get(i);
			if (isGoodItem(item) && !isGoodItem(oldItem))
				break;
		}
		assert item != null;

		// determine the size of the item stack
		int quantity = 1;
		if (item.getMaxStackSize() > 1) {
			for (int i = 0; i <= luck; i++) {
				quantity = Math.max(quantity, RandomUtil.nextInclusiveInt(1, item.getMaxStackSize()));
			}
		}

		// create item stack
		ItemStack itemStack = new ItemStack(item, quantity);
		ItemMeta itemMeta = itemStack.getItemMeta();
		// make item unbreakable with a default chance of 10% (up to 100% at 6 luck)
		if (random.nextDouble() >= (0.9D - (luck * .15D)))
			itemMeta.setUnbreakable(true);

		// determine enchantments to add
		int enchantments = 0;
		for (int i = 0; i <= luck; i++) {
			enchantments = Math.max(enchantments, RandomUtil.weightedRandom(EnchantmentWeights.values(), EnchantmentWeights.TOTAL_WEIGHTS).getLevel());
		}
		List<Enchantment> enchantmentList = Arrays.stream(EnchantmentWrapper.values())
				.filter(enchantment -> enchantment.canEnchantItem(itemStack))
				.collect(Collectors.toList());
		// add enchantments
		if (enchantments > 0 && !enchantmentList.isEmpty()) {
			Collections.shuffle(enchantmentList, random);
			List<Enchantment> addedEnchantments = new ArrayList<>(enchantments);
			int count = 0;
			for (int i = 0; i < enchantmentList.size() && count < enchantments; ++i) {
				Enchantment enchantment = enchantmentList.get(i);
				// block conflicting enchantments (unless the die roll decides otherwise)
				if (addedEnchantments.stream().anyMatch(x -> x.conflictsWith(enchantment)) && random.nextDouble() >= (.1d + (luck * .1d)))
					continue;
				++count;
				addedEnchantments.add(enchantment);
				// determine enchantment level
				int level = enchantment.getStartLevel();
				if (enchantment.getMaxLevel() > enchantment.getStartLevel()) {
					for (int j = 0; j <= luck; j++) {
						level = Math.max(level, RandomUtil.nextInclusiveInt(enchantment.getStartLevel(), enchantment.getMaxLevel()));
					}
					if (random.nextDouble() >= (0.5D - (luck * .07D)))
						level += random.nextInt(4);
				}
				// add enchant
				itemStack.addEnchantment(enchantment, level);
			}
		}

		// determine attributes to add
		int attributes = 0;
		for (int i = 0; i <= luck; i++) {
			attributes = Math.max(attributes, RandomUtil.weightedRandom(AttributeWeights.values(), AttributeWeights.TOTAL_WEIGHTS).getLevel());
		}
		// add attributes
		if (attributes > 0) {
			List<Attribute> attributeList = new ArrayList<>(ATTRIBUTES);
			Collections.shuffle(attributeList, random);
			for (int i = 0; i < attributeList.size() && i < attributes; ++i) {
				Attribute attribute = attributeList.get(i);
				String name = "lootbox_" + attribute.getKey().getKey();
				// determine percent amount for the modifier
				double amount = 0d;
				for (int j = 0; j <= luck; j++) {
					amount = Math.max(amount, (random.nextDouble() * 2) - 1);
				}
				// create & add attribute
				AttributeModifier attributeModifier = new AttributeModifier(name, amount, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
				itemMeta.addAttributeModifier(attribute, attributeModifier);
			}
		}

		// finish up
		itemStack.setItemMeta(itemMeta);
		return itemStack;
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		for (Player player : players) {
			Inventory lootbox = Bukkit.createInventory(null, 27, CommandConstants.buildLootboxTitle(request));
			// create item
			ItemStack randomItem = createRandomItem(0);
			ItemMeta itemMeta = randomItem.getItemMeta();
			itemMeta.lore(Collections.singletonList(CommandConstants.buildLootboxLore(request)));
			randomItem.setItemMeta(itemMeta);
			// display lootbox
			lootbox.setItem(13, randomItem);
			sync(() -> player.openInventory(lootbox));
			player.playSound(
					Sounds.LOOTBOX_CHIME.get(),
					Sound.Emitter.self()
			);
		}
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
