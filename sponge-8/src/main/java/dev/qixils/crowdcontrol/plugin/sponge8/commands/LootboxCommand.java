package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import com.flowpowered.math.vector.Vector3d;
import dev.qixils.crowdcontrol.common.CommandConstants.EnchantmentWeights;
import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.common.util.sound.Sounds;
import dev.qixils.crowdcontrol.exceptions.ExceptionUtil;
import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.property.item.UseLimitProperty;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryCapacity;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static dev.qixils.crowdcontrol.common.CommandConstants.buildLootboxLore;
import static dev.qixils.crowdcontrol.common.CommandConstants.buildLootboxTitle;

@Getter
public class LootboxCommand extends ImmediateCommand {
	private static final InventoryArchetype ARCHETYPE = InventoryArchetype.builder()
			.from(InventoryArchetypes.MENU_GRID)
			.title(Text.of("Lootbox"))
			.property(InventoryCapacity.of(27))
			.build("crowd-control:lootbox", "Lootbox");
	private final List<ItemType> allItems;
	private final List<ItemType> goodItems;
	private final String effectName = "lootbox";
	private final String displayName = "Open Lootbox";

	public LootboxCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
		allItems = new ArrayList<>(plugin.getRegistry().getAllOf(ItemType.class));
		goodItems = allItems.stream()
				.filter(itemType ->
						itemType.getDefaultProperty(UseLimitProperty.class).map(UseLimitProperty::getValue).orElse(0) > 1
								// API8: enchanted golden apple and netherite block
								|| itemType.equals(ItemTypes.GOLDEN_APPLE)
								|| itemType.equals(ItemTypes.DIAMOND_BLOCK)
								|| itemType.equals(ItemTypes.IRON_BLOCK)
								|| itemType.equals(ItemTypes.GOLD_BLOCK))
				.collect(Collectors.toList());
	}

	private boolean isGoodItem(@Nullable ItemType item) {
		return item != null && goodItems.contains(item);
	}

	/**
	 * Creates a random item that is influenced by the supplied luck value.
	 * The item may contain enchantments and modifiers if applicable.
	 *
	 * @param luck zero-indexed level of luck
	 * @return new random item
	 */
	public ItemStack createRandomItem(int luck) {
		// determine the item used in the stack
		// "good" items have a higher likelihood of being picked with positive luck
		List<ItemType> items = new ArrayList<>(allItems);
		Collections.shuffle(items, random);
		ItemType item = null;
		for (int i = 0; i <= luck * 4; i++) {
			ItemType oldItem = item;
			item = items.get(i);
			if (isGoodItem(item) && !isGoodItem(oldItem))
				break;
		}
		assert item != null;

		// determine the size of the item stack
		int quantity = 1;
		if (item.getMaxStackQuantity() > 1) {
			for (int i = 0; i <= luck; i++) {
				quantity = Math.max(quantity, RandomUtil.nextInclusiveInt(1, item.getMaxStackQuantity()));
			}
		}

		// create item stack
		ItemStack itemStack = ItemStack.of(item, quantity);
		// make item unbreakable with a default chance of 10% (up to 100% at 6 luck)
		if (random.nextDouble() >= (0.9D - (luck * .15D)))
			itemStack.offer(Keys.UNBREAKABLE, true);

		// determine enchantments to add
		int enchantments = 0;
		for (int i = 0; i <= luck; i++) {
			enchantments = Math.max(enchantments, RandomUtil.weightedRandom(EnchantmentWeights.values(), EnchantmentWeights.TOTAL_WEIGHTS).getLevel());
		}
		List<EnchantmentType> enchantmentList = plugin.getRegistry().getAllOf(EnchantmentType.class).stream()
				.filter(enchantmentType -> enchantmentType.canBeAppliedToStack(itemStack)).collect(Collectors.toList());
		List<EnchantmentType> addedEnchantments = new ArrayList<>(enchantments);

		// add enchantments
		while (enchantments > 0 && !enchantmentList.isEmpty()) {
			EnchantmentType enchantment = enchantmentList.remove(0);

			// block conflicting enchantments (unless the die roll decides otherwise)
			if (addedEnchantments.stream().anyMatch(x -> !x.isCompatibleWith(enchantment)) && random.nextDouble() >= (.1d + (luck * .1d)))
				continue;
			addedEnchantments.add(enchantment);
			enchantments--;

			// determine enchantment level
			int level = enchantment.getMinimumLevel();
			if (enchantment.getMaximumLevel() > enchantment.getMinimumLevel()) {
				for (int j = 0; j <= luck; j++) {
					level = Math.max(level, RandomUtil.nextInclusiveInt(enchantment.getMinimumLevel(), enchantment.getMaximumLevel()));
				}
				if (random.nextDouble() >= (0.5D - (luck * .07D)))
					level += random.nextInt(4);
			}

			// create & add enchant
			Enchantment builtEnchantment = Enchantment.builder()
					.type(enchantment)
					.level(level)
					.build();
			itemStack.transform(Keys.ITEM_ENCHANTMENTS, enchants -> {
				enchants = ExceptionUtil.validateNotNullElseGet(enchants, ArrayList::new);
				enchants.add(builtEnchantment);
				return enchants;
			});
		}

		// API8: attributes
		// (attributes kinda exist in Sponge 7 but they are not available as a CatalogType, just
		//  as Keys, and even then I don't think I can add them to items)

		return itemStack;
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		for (Player player : players) {
			Inventory lootbox = Inventory.builder()
					.of(ARCHETYPE)
					.property(new InventoryTitle(spongeSerializer.serialize(buildLootboxTitle(request))))
					.build(plugin);

			// create item
			ItemStack itemStack = createRandomItem(0);
			itemStack.offer(
					Keys.ITEM_LORE,
					Collections.singletonList(spongeSerializer.serialize(buildLootboxLore(request)))
			);

			// the custom inventory does not implement anything sensible so enjoy this hack
			int i = 0;
			for (Inventory slot : lootbox.slots()) {
				if (i++ == 13) {
					slot.offer(itemStack);
					break;
				}
			}
			// sound & open
			Vector3d pos = player.getPosition();
			plugin.asAudience(player).playSound(Sounds.LOOTBOX_CHIME.get(), pos.getX(), pos.getY(), pos.getZ());
			sync(() -> player.openInventory(lootbox));
		}
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
