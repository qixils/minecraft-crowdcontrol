package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.common.command.CommandConstants.AttributeWeights;
import dev.qixils.crowdcontrol.common.command.CommandConstants.EnchantmentWeights;
import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.common.util.sound.Sounds;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.buildLootboxLore;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.buildLootboxTitle;
import static net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson;
import static net.minecraft.world.entity.EquipmentSlot.MAINHAND;
import static net.minecraft.world.entity.EquipmentSlot.OFFHAND;

@Getter
public class LootboxCommand extends ImmediateCommand {
	private static final List<Attribute> ATTRIBUTES = Arrays.asList(
			Attributes.MAX_HEALTH,
			Attributes.KNOCKBACK_RESISTANCE,
			Attributes.MOVEMENT_SPEED,
			Attributes.ATTACK_DAMAGE,
			Attributes.ARMOR,
			Attributes.ARMOR_TOUGHNESS,
			Attributes.ATTACK_KNOCKBACK,
			Attributes.ATTACK_SPEED
	);
	private final List<Item> allItems;
	private final List<Item> goodItems;
	private final String effectName;
	private final int luck;

	public LootboxCommand(FabricCrowdControlPlugin plugin, int luck) {
		// init basic variables
		super(plugin);
		this.luck = luck;

		// set effect name to an ID like "lootbox_5" or just "lootbox" for luck level of 1
		StringBuilder effectName = new StringBuilder("lootbox");
		if (luck > 0)
			effectName.append('_').append(luck);
		this.effectName = effectName.toString();

		// create item collections
		allItems = BuiltInRegistries.ITEM.stream().filter(it -> it != Items.AIR).toList();
		goodItems = allItems.stream()
				.filter(itemType ->
						itemType.getMaxDamage() > 1
								|| itemType == Items.GOLDEN_APPLE
								|| itemType == Items.ENCHANTED_GOLDEN_APPLE
								|| itemType == Items.NETHERITE_BLOCK
								|| itemType == Items.DIAMOND_BLOCK
								|| itemType == Items.IRON_BLOCK
								|| itemType == Items.GOLD_BLOCK)
				.collect(Collectors.toList());
	}

	private boolean isGoodItem(@Nullable Item item) {
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
		List<Item> items = new ArrayList<>(allItems);
		items.removeIf(plugin::isDisabled);
		Collections.shuffle(items, random);
		Item item = null;
		for (int i = 0; i <= luck * 5; i++) {
			Item oldItem = item;
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
		randomlyModifyItem(itemStack, luck);
		return itemStack;
	}

	/**
	 * Applies various random modifications to an item including enchantments, attributes, and
	 * unbreaking.
	 *
	 * @param itemStack item to modify
	 * @param luck      zero-indexed level of luck
	 */
	@Contract(mutates = "param1")
	public void randomlyModifyItem(ItemStack itemStack, int luck) {
		// make item unbreakable with a default chance of 10% (up to 100% at 6 luck)
		if (random.nextDouble() >= (0.9D - (luck * .15D)))
			itemStack.getOrCreateTag().putBoolean("Unbreakable", true);

		// determine enchantments to add
		int _enchantments = 0;
		for (int i = 0; i <= luck; i++) {
			_enchantments = Math.max(_enchantments, RandomUtil.weightedRandom(EnchantmentWeights.values(), EnchantmentWeights.TOTAL_WEIGHTS).getLevel());
		}
		final int enchantments = _enchantments;
		List<Enchantment> enchantmentList = BuiltInRegistries.ENCHANTMENT.stream()
				.filter(enchantmentType -> enchantmentType.canEnchant(itemStack)).collect(Collectors.toList());
		if (random.nextDouble() >= (.8d - (luck * .2d)))
			enchantmentList.removeIf(Enchantment::isCurse);

		// add enchantments
		List<Enchantment> addedEnchantments = new ArrayList<>(enchantments);
		while (addedEnchantments.size() < enchantments && !enchantmentList.isEmpty()) {
			Enchantment enchantment = enchantmentList.remove(0);

			// block conflicting enchantments (unless the die roll decides otherwise)
			if (addedEnchantments.stream().anyMatch(x -> !x.isCompatibleWith(enchantment)) && random.nextDouble() >= (.1d + (luck * .1d)))
				continue;
			addedEnchantments.add(enchantment);

			// determine enchantment level
			int level = enchantment.getMinLevel();
			if (enchantment.getMaxLevel() > enchantment.getMinLevel()) {
				for (int j = 0; j <= luck; j++) {
					level = Math.max(level, RandomUtil.nextInclusiveInt(enchantment.getMinLevel(), enchantment.getMaxLevel()));
				}
				if (random.nextDouble() >= (0.5D - (luck * .07D)))
					level += random.nextInt(4);
			}

			// create & add enchant
			itemStack.enchant(enchantment, level);
		}

		// determine attributes to add
		int attributes = 0;
		for (int i = 0; i <= luck; i++) {
			attributes = Math.max(attributes, RandomUtil.weightedRandom(AttributeWeights.values(), AttributeWeights.TOTAL_WEIGHTS).getLevel());
		}
		// add attributes
		if (attributes > 0) {
			// get equipment slot(s)
			EquipmentSlot equipmentSlot = itemStack.getItem() instanceof ArmorItem armorItem ? armorItem.getEquipmentSlot() : null;
			EquipmentSlot[] equipmentSlots = equipmentSlot == null ? new EquipmentSlot[]{MAINHAND, OFFHAND} : new EquipmentSlot[]{equipmentSlot};
			// add custom attributes
			List<Attribute> attributeList = new ArrayList<>(ATTRIBUTES);
			Collections.shuffle(attributeList, random);
			for (int i = 0; i < attributeList.size() && i < attributes; i++) {
				Attribute attribute = attributeList.get(i);
				String name = "lootbox_" + Objects.requireNonNull(BuiltInRegistries.ATTRIBUTE.getKey(attribute)).getPath();
				// determine percent amount for the modifier
				double amount = 0d;
				for (int j = 0; j <= luck; j++) {
					amount = Math.max(amount, (random.nextDouble() * 2) - 1);
				}
				// create & add attribute
				for (int k = 0; k < equipmentSlots.length; k++) {
					AttributeModifier attributeModifier = new AttributeModifier(UUID.randomUUID(), name + "_" + k, amount, Operation.ADDITION);
					itemStack.addAttributeModifier(attribute, attributeModifier, equipmentSlots[k]);
				}
			}
			// add default attributes
			for (EquipmentSlot type : equipmentSlots) {
				itemStack.getItem().getDefaultAttributeModifiers(type)
						.forEach((attribute, modifiers) -> itemStack.addAttributeModifier(attribute, modifiers, type));
			}
		}
	}

	// lore getter
	private static List<net.kyori.adventure.text.Component> getLore(ItemStack itemStack) {
		final CompoundTag displayTag = itemStack.getTag();
		if (displayTag == null || !displayTag.contains("display"))
			return new ArrayList<>();
		final CompoundTag displayCompound = displayTag.getCompound("display");
		final ListTag list = displayCompound.getList("Lore", 8);
		return list.isEmpty() ? new ArrayList<>() : list.stream().map(tag -> gson().deserialize(tag.getAsString())).collect(Collectors.toList());
	}

	// lore setter
	private static void setLore(ItemStack itemStack, List<net.kyori.adventure.text.Component> lore) {
		if (lore.isEmpty()) {
			final CompoundTag tag = itemStack.getTag();
			if (tag != null && tag.contains("display"))
				tag.getCompound("display").remove("Lore");
			return;
		}
		final ListTag list = new ListTag();
		lore.forEach(component -> list.add(StringTag.valueOf(gson().serialize(component))));
		itemStack.getOrCreateTagElement("display").put("Lore", list);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		for (ServerPlayer player : players) {
			// init container
			SimpleContainer container = new SimpleContainer(27);
			for (int slot : CommandConstants.lootboxItemSlots(luck)) {
				ItemStack itemStack = createRandomItem(luck);
				List<Component> lore = getLore(itemStack);
				lore.add(plugin.adventure().renderer().render(buildLootboxLore(plugin, request), player));
				setLore(itemStack, lore);
				container.setItem(slot, itemStack);
			}

			// sound & open
			player.playSound(Sounds.LOOTBOX_CHIME.get(luck), Sound.Emitter.self());
			sync(() -> player.openMenu(
					new SimpleMenuProvider((i, inventory, p) -> ChestMenu.threeRows(i, inventory, container),
							plugin.adventure().toNative(buildLootboxTitle(plugin, request)))
			));
		}
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
