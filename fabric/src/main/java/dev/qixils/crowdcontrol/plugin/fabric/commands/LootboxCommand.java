package dev.qixils.crowdcontrol.plugin.fabric.commands;

import com.google.common.collect.Lists;
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
import net.kyori.adventure.text.ComponentLike;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.buildLootboxLore;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.buildLootboxTitle;
import static dev.qixils.crowdcontrol.plugin.fabric.utils.AttributeUtil.migrateId;
import static net.minecraft.world.item.enchantment.EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE;
import static net.minecraft.world.item.enchantment.EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP;

@Getter
public class LootboxCommand extends ImmediateCommand {
	private static final List<Holder<Attribute>> ATTRIBUTES = Arrays.asList(
		Attributes.MAX_HEALTH,
		Attributes.KNOCKBACK_RESISTANCE,
		Attributes.MOVEMENT_SPEED,
		Attributes.ATTACK_DAMAGE,
		Attributes.ARMOR,
		Attributes.ARMOR_TOUGHNESS,
		Attributes.ATTACK_KNOCKBACK,
		Attributes.ATTACK_SPEED
	);
	private static final List<DataComponentType<?>> CURSES = List.of(
		PREVENT_ARMOR_CHANGE,
		PREVENT_EQUIPMENT_DROP
	);
	private static final Map<EquipmentSlot, EquipmentSlotGroup> SLOT_TO_GROUP = Map.of(
		// can't use reflection cus mappings (probably)
		EquipmentSlot.MAINHAND, EquipmentSlotGroup.MAINHAND,
		EquipmentSlot.OFFHAND, EquipmentSlotGroup.OFFHAND,
		EquipmentSlot.HEAD, EquipmentSlotGroup.HEAD,
		EquipmentSlot.CHEST, EquipmentSlotGroup.CHEST,
		EquipmentSlot.LEGS, EquipmentSlotGroup.LEGS,
		EquipmentSlot.FEET, EquipmentSlotGroup.FEET
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
				itemType.components().getOrDefault(DataComponents.MAX_DAMAGE, 0) > 1
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
		int maxStackSize = item.components().getOrDefault(DataComponents.MAX_STACK_SIZE, 0);
		if (maxStackSize > 1) {
			for (int i = 0; i <= luck; i++) {
				quantity = Math.max(quantity, RandomUtil.nextInclusiveInt(1, maxStackSize));
			}
		}

		// create item stack
		ItemStack itemStack = new ItemStack(item, quantity);
		randomlyModifyItem(itemStack, luck, null);
		return itemStack;
	}

	/**
	 * Applies various random modifications to an item including enchantments, attributes, and
	 * unbreaking.
	 *
	 * @param itemStack item to modify
	 * @param luck      zero-indexed level of luck
	 * @param accessor  registry accessor to load enchants from
	 */
	@Contract(mutates = "param1")
	public void randomlyModifyItem(ItemStack itemStack, int luck, @Nullable RegistryAccess accessor) {
		// make item unbreakable with a default chance of 10% (up to 100% at 6 luck)
		if (random.nextDouble() >= (0.9D - (luck * .15D)))
			itemStack.set(DataComponents.UNBREAKABLE, new Unbreakable(true));

		// determine enchantments to add
		int _enchantments = 0;
		for (int i = 0; i <= luck; i++) {
			_enchantments = Math.max(_enchantments, RandomUtil.weightedRandom(EnchantmentWeights.values(), EnchantmentWeights.TOTAL_WEIGHTS).getLevel());
		}
		final int enchantments = _enchantments;
		List<Holder<Enchantment>> enchantmentList = plugin.registry(Registries.ENCHANTMENT, accessor)
			.holders()
			.filter(enchantmentHolder -> enchantmentHolder.value().canEnchant(itemStack))
			.collect(Collectors.toList());
		if (random.nextDouble() >= (.8d - (luck * .2d))) {
			for (DataComponentType<?> curse : CURSES) {
				enchantmentList.removeIf(holder -> holder.value().effects().has(curse));
			}
		}

		// add enchantments
		List<Holder<Enchantment>> addedEnchantments = new ArrayList<>(enchantments);
		while (addedEnchantments.size() < enchantments && !enchantmentList.isEmpty()) {
			Holder<Enchantment> enchantment = enchantmentList.remove(0);

			// block conflicting enchantments (unless the die roll decides otherwise)
			if (addedEnchantments.stream().anyMatch(x -> Enchantment.areCompatible(x, enchantment)) && random.nextDouble() >= (.1d + (luck * .1d)))
				continue;
			addedEnchantments.add(enchantment);

			// determine enchantment level
			int level = enchantment.value().getMinLevel();
			if (enchantment.value().getMaxLevel() > level) {
				for (int j = 0; j <= luck; j++) {
					level = Math.max(level, RandomUtil.nextInclusiveInt(enchantment.value().getMinLevel(), enchantment.value().getMaxLevel()));
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
			EquipmentSlotGroup _equipmentSlotGroup = equipmentSlot == null ? null : SLOT_TO_GROUP.get(equipmentSlot);
			final EquipmentSlotGroup equipmentSlotGroup = _equipmentSlotGroup == null ? EquipmentSlotGroup.HAND : _equipmentSlotGroup;
			// add custom attributes
			List<Holder<Attribute>> attributeList = new ArrayList<>(ATTRIBUTES);
			Collections.shuffle(attributeList, random);
			for (int i = 0; i < attributeList.size() && i < attributes; i++) {
				Holder<Attribute> attribute = attributeList.get(i);
				// determine percent amount for the modifier
				double amount = 0d;
				for (int j = 0; j <= luck; j++) {
					amount = Math.max(amount, (random.nextDouble() * 2) - 1);
				}
				// create & add attribute
				AttributeModifier attributeModifier = new AttributeModifier(migrateId(UUID.randomUUID()), amount, Operation.ADD_VALUE);
				ItemAttributeModifiers modifiers = itemStack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
				modifiers = modifiers.withModifierAdded(attribute, attributeModifier, equipmentSlotGroup);
				itemStack.set(DataComponents.ATTRIBUTE_MODIFIERS, modifiers);
			}
			// add default attributes
			for (EquipmentSlot type : EquipmentSlot.values()) {
				if (!equipmentSlotGroup.test(type)) continue;
				itemStack.getItem().getDefaultAttributeModifiers()
					.forEach(type, (attribute, modifier) -> {
						// TODO: does any of this make sense (especially the equipmentSlotGroup)
						ItemAttributeModifiers modifiers = itemStack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
						modifiers = modifiers.withModifierAdded(attribute, modifier, equipmentSlotGroup);
						itemStack.set(DataComponents.ATTRIBUTE_MODIFIERS, modifiers);
					});
			}
		}
	}

	// lore getter
	private static List<net.kyori.adventure.text.Component> getLore(ItemStack itemStack) {
		ItemLore itemLore = itemStack.get(DataComponents.LORE);
		if (itemLore == null || itemLore.lines().isEmpty())
			return new ArrayList<>();
		return itemLore.lines().stream().map(ComponentLike::asComponent).collect(Collectors.toList());
	}

	// lore setter
	private void setLore(ItemStack itemStack, List<net.kyori.adventure.text.Component> lore) {
		if (lore.isEmpty()) {
			itemStack.remove(DataComponents.LORE);
			return;
		}
		List<net.minecraft.network.chat.Component> nativeLore = Lists.transform(lore, component -> plugin.adventure().toNative(component));
		itemStack.set(DataComponents.LORE, new ItemLore(nativeLore));
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
