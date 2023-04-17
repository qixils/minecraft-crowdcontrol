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
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.buildLootboxLore;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.buildLootboxTitle;
import static net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson;
import static net.minecraft.entity.EquipmentSlot.MAINHAND;
import static net.minecraft.entity.EquipmentSlot.OFFHAND;

@Getter
public class LootboxCommand extends ImmediateCommand {
	private static final List<EntityAttribute> ATTRIBUTES = Arrays.asList(
			EntityAttributes.GENERIC_MAX_HEALTH,
			EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,
			EntityAttributes.GENERIC_MOVEMENT_SPEED,
			EntityAttributes.GENERIC_ATTACK_DAMAGE,
			EntityAttributes.GENERIC_ARMOR,
			EntityAttributes.GENERIC_ARMOR_TOUGHNESS,
			EntityAttributes.GENERIC_ATTACK_KNOCKBACK,
			EntityAttributes.GENERIC_ATTACK_SPEED
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
		allItems = Registries.ITEM.stream().toList();
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
		if (item.getMaxCount() > 1) {
			for (int i = 0; i <= luck; i++) {
				quantity = Math.max(quantity, RandomUtil.nextInclusiveInt(1, item.getMaxCount()));
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
			itemStack.getOrCreateNbt().putBoolean("Unbreakable", true);

		// determine enchantments to add
		int _enchantments = 0;
		for (int i = 0; i <= luck; i++) {
			_enchantments = Math.max(_enchantments, RandomUtil.weightedRandom(EnchantmentWeights.values(), EnchantmentWeights.TOTAL_WEIGHTS).getLevel());
		}
		final int enchantments = _enchantments;
		List<Enchantment> enchantmentList = Registries.ENCHANTMENT.stream()
				.filter(enchantmentType -> enchantmentType.isAcceptableItem(itemStack)).collect(Collectors.toList());
		if (random.nextDouble() >= (.8d - (luck * .2d)))
			enchantmentList.removeIf(Enchantment::isCursed);

		// add enchantments
		List<Enchantment> addedEnchantments = new ArrayList<>(enchantments);
		while (addedEnchantments.size() < enchantments && !enchantmentList.isEmpty()) {
			Enchantment enchantment = enchantmentList.remove(0);

			// block conflicting enchantments (unless the die roll decides otherwise)
			if (addedEnchantments.stream().anyMatch(x -> !x.canCombine(enchantment)) && random.nextDouble() >= (.1d + (luck * .1d)))
				continue;
			addedEnchantments.add(enchantment);

			// determine enchantment level
			int level = enchantment.getMinLevel();
			if (enchantment.getMaxLevel() > enchantment.getMaxLevel()) {
				for (int j = 0; j <= luck; j++) {
					level = Math.max(level, RandomUtil.nextInclusiveInt(enchantment.getMinLevel(), enchantment.getMaxLevel()));
				}
				if (random.nextDouble() >= (0.5D - (luck * .07D)))
					level += random.nextInt(4);
			}

			// create & add enchant
			itemStack.addEnchantment(enchantment, level);
		}

		// determine attributes to add
		int attributes = 0;
		for (int i = 0; i <= luck; i++) {
			attributes = Math.max(attributes, RandomUtil.weightedRandom(AttributeWeights.values(), AttributeWeights.TOTAL_WEIGHTS).getLevel());
		}
		// add attributes
		if (attributes > 0) {
			// get equipment slot(s)
			EquipmentSlot equipmentSlot = itemStack.getItem() instanceof ArmorItem armorItem ? armorItem.getSlotType() : null;
			EquipmentSlot[] equipmentSlots = equipmentSlot == null ? new EquipmentSlot[]{MAINHAND, OFFHAND} : new EquipmentSlot[]{equipmentSlot};
			// add custom attributes
			List<EntityAttribute> attributeList = new ArrayList<>(ATTRIBUTES);
			Collections.shuffle(attributeList, random);
			for (int i = 0; i < attributeList.size() && i < attributes; i++) {
				EntityAttribute attribute = attributeList.get(i);
				String name = "lootbox_" + Objects.requireNonNull(Registries.ATTRIBUTE.getId(attribute)).getPath();
				// determine percent amount for the modifier
				double amount = 0d;
				for (int j = 0; j <= luck; j++) {
					amount = Math.max(amount, (random.nextDouble() * 2) - 1);
				}
				// create & add attribute
				for (int k = 0; k < equipmentSlots.length; k++) {
					EntityAttributeModifier attributeModifier = new EntityAttributeModifier(UUID.randomUUID(), name + "_" + k, amount, Operation.ADDITION);
					itemStack.addAttributeModifier(attribute, attributeModifier, equipmentSlots[k]);
				}
			}
			// add default attributes
			for (EquipmentSlot type : equipmentSlots) {
				itemStack.getItem().getAttributeModifiers(type)
						.forEach((attribute, modifiers) -> itemStack.addAttributeModifier(attribute, modifiers, type));
			}
		}
	}

	// lore getter
	private static List<net.kyori.adventure.text.Component> getLore(ItemStack itemStack) {
		final NbtCompound displayTag = itemStack.getNbt();
		if (displayTag == null || !displayTag.contains("display"))
			return new ArrayList<>();
		final NbtCompound displayCompound = displayTag.getCompound("display");
		final NbtList list = displayCompound.getList("Lore", 8);
		return list.isEmpty() ? new ArrayList<>() : list.stream().map(tag -> gson().deserialize(tag.asString())).collect(Collectors.toList());
	}

	// lore setter
	private static void setLore(ItemStack itemStack, List<net.kyori.adventure.text.Component> lore) {
		if (lore.isEmpty()) {
			final NbtCompound tag = itemStack.getNbt();
			if (tag != null && tag.contains("display"))
				tag.getCompound("display").remove("Lore");
			return;
		}
		final NbtList list = new NbtList();
		lore.forEach(component -> list.add(NbtString.of(gson().serialize(component))));
		itemStack.getOrCreateSubNbt("display").put("Lore", list);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayerEntity> players, @NotNull Request request) {
		for (ServerPlayerEntity player : players) {
			// init container
			SimpleInventory container = new SimpleInventory(27);
			for (int slot : CommandConstants.lootboxItemSlots(luck)) {
				ItemStack itemStack = createRandomItem(luck);
				List<Component> lore = getLore(itemStack);
				lore.add(plugin.adventure().renderer().render(buildLootboxLore(plugin, request), player));
				setLore(itemStack, lore);
				container.setStack(slot, itemStack);
			}

			// sound & open
			player.playSound(Sounds.LOOTBOX_CHIME.get(luck), Sound.Emitter.self());
			sync(() -> player.openHandledScreen(
					new SimpleNamedScreenHandlerFactory((i, inventory, p) -> GenericContainerScreenHandler.createGeneric9x3(i, inventory, container),
							plugin.adventure().toNative(buildLootboxTitle(plugin, request)))
			));
		}
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
