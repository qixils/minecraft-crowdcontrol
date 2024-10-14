package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.common.ExecuteUsing;
import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.common.util.sound.Sounds;
import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.attribute.AttributeModifier;
import org.spongepowered.api.entity.attribute.AttributeOperations;
import org.spongepowered.api.entity.attribute.type.AttributeType;
import org.spongepowered.api.entity.attribute.type.AttributeTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.item.inventory.container.InteractContainerEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.ContainerTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.api.item.inventory.type.ViewableInventory;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.plugin.PluginContainer;

import java.util.*;
import java.util.stream.Collectors;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.*;

@ExecuteUsing(ExecuteUsing.Type.SYNC_GLOBAL)
@Getter
public class LootboxCommand extends ImmediateCommand {
	private static final @NotNull EquipmentType MAIN_HAND = EquipmentTypes.MAIN_HAND.get();
	private static final @NotNull EquipmentType OFF_HAND = EquipmentTypes.OFF_HAND.get();
	private static final List<AttributeType> ATTRIBUTES = Arrays.asList(
			AttributeTypes.GENERIC_MAX_HEALTH.get(),
			AttributeTypes.GENERIC_KNOCKBACK_RESISTANCE.get(),
			AttributeTypes.GENERIC_MOVEMENT_SPEED.get(),
			AttributeTypes.GENERIC_ATTACK_DAMAGE.get(),
			AttributeTypes.GENERIC_ARMOR.get(),
			AttributeTypes.GENERIC_ARMOR_TOUGHNESS.get(),
			AttributeTypes.GENERIC_ATTACK_KNOCKBACK.get(),
			AttributeTypes.GENERIC_ATTACK_SPEED.get()
	);
	// some @Data class would make more sense than 3 hashmaps lol
	private static final Map<UUID, ViewableInventory> OPEN_LOOTBOXES = new HashMap<>();
	private static final Map<UUID, Component> TITLES = new HashMap<>();
	private static final Map<UUID, Container> CONTAINERS = new HashMap<>();
	private final List<ItemType> allItems;
	private final List<ItemType> goodItems;
	private final String effectName;
	private final int luck;

	public LootboxCommand(SpongeCrowdControlPlugin plugin, int luck) {
		// init basic variables
		super(plugin);
		this.luck = luck;

		// set effect name to an ID like "lootbox_5" or just "lootbox" for luck level of 1
		StringBuilder effectName = new StringBuilder("lootbox");
		if (luck > 0)
			effectName.append('_').append(luck);
		this.effectName = effectName.toString();

		// create item collections
		allItems = plugin.getGame().registry(RegistryTypes.ITEM_TYPE).stream().filter(it -> it != ItemTypes.AIR.get()).collect(Collectors.toList());
		goodItems = allItems.stream()
				.filter(itemType ->
						ItemStack.of(itemType).get(Keys.MAX_DURABILITY).orElse(0) > 1
								|| itemType.equals(ItemTypes.GOLDEN_APPLE.get())
								|| itemType.equals(ItemTypes.ENCHANTED_GOLDEN_APPLE.get())
								|| itemType.equals(ItemTypes.NETHERITE_BLOCK.get())
								|| itemType.equals(ItemTypes.DIAMOND_BLOCK.get())
								|| itemType.equals(ItemTypes.IRON_BLOCK.get())
								|| itemType.equals(ItemTypes.GOLD_BLOCK.get()))
				.collect(Collectors.toList());
	}

	private static boolean isLootboxOpen(@NotNull UUID uuid, boolean fromClose) {
		if (!OPEN_LOOTBOXES.containsKey(uuid)) return false;
		Inventory inventory = OPEN_LOOTBOXES.get(uuid);
		Container container = CONTAINERS.get(uuid);
		if (inventory.totalQuantity() == 0 || (!container.isOpen() && !fromClose)) {
			OPEN_LOOTBOXES.remove(uuid);
			return false;
		}
		return true;
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
		for (int i = 0; i <= luck * 5; i++) {
			ItemType oldItem = item;
			item = items.get(i);
			if (isGoodItem(item) && !isGoodItem(oldItem))
				break;
		}
		assert item != null;

		// determine the size of the item stack
		int quantity = 1;
		if (item.maxStackQuantity() > 1) {
			for (int i = 0; i <= luck; i++) {
				quantity = Math.max(quantity, RandomUtil.nextInclusiveInt(1, item.maxStackQuantity()));
			}
		}

		// create item stack
		ItemStack itemStack = ItemStack.of(item, quantity);
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
		// make item unbreakable with a default chance of 5% (up to 100% at 10 luck)
		if (random.nextDouble() >= (UNBREAKABLE_BASE - (luck * UNBREAKABLE_DEC)))
			itemStack.offer(Keys.IS_UNBREAKABLE, true);

		// determine enchantments to add
		int _enchantments = 0;
		for (int i = 0; i <= luck; i++) {
			_enchantments = Math.max(_enchantments, RandomUtil.weightedRandom(EnchantmentWeights.values(), EnchantmentWeights.TOTAL_WEIGHTS).getLevel());
		}
		final int enchantments = _enchantments;
		List<EnchantmentType> enchantmentList = plugin.getGame().registry(RegistryTypes.ENCHANTMENT_TYPE).stream()
				.filter(enchantmentType -> enchantmentType.canBeAppliedToStack(itemStack)).collect(Collectors.toList());
		if (random.nextDouble() >= (.8d - (luck * .2d)))
			enchantmentList.removeIf(EnchantmentType::isCurse);

		// add enchantments
		List<EnchantmentType> addedEnchantments = new ArrayList<>(enchantments);
		while (addedEnchantments.size() < enchantments && !enchantmentList.isEmpty()) {
			EnchantmentType enchantment = enchantmentList.remove(0);

			// block conflicting enchantments (unless the die roll decides otherwise)
			if (addedEnchantments.stream().anyMatch(x -> !x.isCompatibleWith(enchantment)) && random.nextDouble() >= (.1d + (luck * .1d)))
				continue;
			addedEnchantments.add(enchantment);

			// determine enchantment level
			int level = enchantment.minimumLevel();
			if (enchantment.maximumLevel() > level) {
				for (int j = 0; j <= luck; j++) {
					level = Math.max(level, RandomUtil.nextInclusiveInt(enchantment.minimumLevel(), enchantment.maximumLevel()));
				}
				if (random.nextDouble() >= (0.5D - (luck * .07D)))
					level += random.nextInt(4);
			}

			// create & add enchant
			Enchantment builtEnchantment = Enchantment.builder()
					.type(enchantment)
					.level(level)
					.build();
			itemStack.transform(Keys.APPLIED_ENCHANTMENTS, enchants -> {
				enchants = enchants == null
					? new ArrayList<>()
					: new ArrayList<>(enchants);
				enchants.add(builtEnchantment);
				return enchants;
			});
		}

		// determine attributes to add
		int attributes = 0;
		for (int i = 0; i <= luck; i++) {
			attributes = Math.max(attributes, RandomUtil.weightedRandom(AttributeWeights.values(), AttributeWeights.TOTAL_WEIGHTS).getLevel());
		}
		// add attributes
		if (attributes > 0
				&& itemStack.getOrElse(Keys.MAX_DURABILITY, 0) <= 1 // TODO: add default attributes to items and remove this
		) {
			List<AttributeType> attributeList = new ArrayList<>(ATTRIBUTES);
			Collections.shuffle(attributeList, random);
			for (int i = 0; i < attributeList.size() && i < attributes; ++i) {
				AttributeType attribute = attributeList.get(i);
				String name = "lootbox_" + attribute.key(RegistryTypes.ATTRIBUTE_TYPE).value();
				// determine percent amount for the modifier
				double amount = 0d;
				for (int j = 0; j <= luck; j++) {
					amount = Math.max(amount, (random.nextDouble() * 2) - 1);
				}
				// create & add attribute
				AttributeModifier attributeModifier = AttributeModifier.builder()
						.name(name)
						.randomId()
						.amount(amount)
						.operation(AttributeOperations.MULTIPLY_TOTAL)
						.build();
				EquipmentType equipmentType = itemStack.get(Keys.EQUIPMENT_TYPE).orElse(MAIN_HAND);
				itemStack.addAttributeModifier(attribute, attributeModifier, equipmentType);
				if (equipmentType.equals(MAIN_HAND)) {
					AttributeModifier offHandAttributeModifier = AttributeModifier.builder()
							.name(name + "_offhand")
							.randomId()
							.amount(amount)
							.operation(AttributeOperations.MULTIPLY_TOTAL)
							.build();
					itemStack.addAttributeModifier(attribute, offHandAttributeModifier, OFF_HAND);
				}
			}
		}
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		boolean success = false;
		for (ServerPlayer player : players) {
			if (isLootboxOpen(player.uniqueId(), false)) continue;

			Inventory baseInventory = Inventory.builder()
					.grid(9, 3)
					.completeStructure()
					.plugin(plugin.getPluginContainer())
					.build();

			ViewableInventory inventory = ViewableInventory.builder()
					.type(ContainerTypes.GENERIC_9X3)
					.slots(baseInventory.slots(), 0)
					.completeStructure()
					.plugin(plugin.getPluginContainer())
					.build();

			// add items
			for (int slot : CommandConstants.lootboxItemSlots(luck)) {
				// create item
				ItemStack itemStack = createRandomItem(luck);
				itemStack.offer(
						Keys.LORE,
						Collections.singletonList(GlobalTranslator.render(buildLootboxLore(plugin, request), player.locale()))
				);

				inventory.slot(slot)
						.orElseThrow(() -> new IllegalStateException("Could not find requested inventory slot " + slot))
						.set(itemStack);
			}

			// sound & open
			Component title = buildLootboxTitle(plugin, request);
			Container container = player.openInventory(inventory, title).orElse(null);
			if (container == null) continue;

			player.playSound(Sounds.LOOTBOX_CHIME.get(luck), Sound.Emitter.self());
			TITLES.put(player.uniqueId(), title);
			OPEN_LOOTBOXES.put(player.uniqueId(), inventory);
			CONTAINERS.put(player.uniqueId(), container);

			success = true;
		}
		return success
			? request.buildResponse().type(Response.ResultType.SUCCESS)
			: request.buildResponse().type(Response.ResultType.RETRY).message("Player has another lootbox open");
	}

	@RequiredArgsConstructor
	public static final class Manager {
		private final SpongeCrowdControlPlugin plugin;

		@Listener
		public void onInventoryClose(InteractContainerEvent.Close event) {
			ServerPlayer player = event.container().viewer();
			UUID uuid = player.uniqueId();

			if (event.cause().containsType(PluginContainer.class)) {
				OPEN_LOOTBOXES.remove(uuid);
				return;
			}

			if (!isLootboxOpen(uuid, true))
				return;

			ViewableInventory lootbox = OPEN_LOOTBOXES.get(uuid);
			Component title = TITLES.get(uuid);
			plugin.getSyncExecutor().execute(() -> player.openInventory(lootbox, title).ifPresent(inv -> CONTAINERS.put(uuid, inv)));
		}
	}
}
