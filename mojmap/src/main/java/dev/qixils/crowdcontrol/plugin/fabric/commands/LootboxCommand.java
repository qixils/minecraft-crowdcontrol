package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.common.util.sound.Sounds;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.*;

@Getter
public class LootboxCommand extends ModdedCommand {
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
	public static final Map<UUID, ChestMenu> OPEN_LOOTBOXES = new HashMap<>();
	public static final Map<UUID, net.minecraft.network.chat.Component> TITLES = new HashMap<>();
	private final List<Item> allItems;
	private final List<Item> goodItems;
	private final String effectName;
	private final int luck;

	public LootboxCommand(ModdedCrowdControlPlugin plugin, int luck) {
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

	private static boolean isLootboxOpen(@NotNull Player player) {
		UUID uuid = player.getUUID();
		if (!OPEN_LOOTBOXES.containsKey(uuid)) return false;
		ChestMenu inv = OPEN_LOOTBOXES.get(uuid);
		if (inv.getContainer().isEmpty() || !inv.stillValid(player) || player.containerMenu != inv) {
			OPEN_LOOTBOXES.remove(uuid);
			return false;
		}
		return true;
	}

	public static void onInventoryClose(ServerPlayer player) {
		if (!ModdedCrowdControlPlugin.isInstanceAvailable()) return;
		ModdedCrowdControlPlugin plugin = ModdedCrowdControlPlugin.getInstance();

		UUID uuid = player.getUUID();

		if (!isLootboxOpen(player))
			return;

		ChestMenu lootbox = OPEN_LOOTBOXES.get(uuid);
		plugin.getSyncExecutor().execute(() -> {
			OptionalInt status = player.openMenu(new SimpleMenuProvider((i, inventory, p) -> ChestMenu.threeRows(i, inventory, lootbox.getContainer()), TITLES.get(uuid)));
			if (status.isEmpty()) OPEN_LOOTBOXES.remove(uuid);
			OPEN_LOOTBOXES.put(uuid, (ChestMenu) player.containerMenu);
		});
	}

	private boolean isGoodItem(@Nullable Item item) {
		return item != null && goodItems.contains(item);
	}

	/**
	 * Creates a random item that is influenced by the supplied luck value.
	 * The item may contain enchantments and modifiers if applicable.
	 *
	 * @param luck           zero-indexed level of luck
	 * @param registryAccess access to a registry
	 * @return new random item
	 */
	public ItemStack createRandomItem(int luck, @Nullable RegistryAccess registryAccess) {
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
		int maxStackSize = item.getMaxStackSize();
		if (maxStackSize > 1) {
			for (int i = 0; i <= luck; i++) {
				quantity = Math.max(quantity, RandomUtil.nextInclusiveInt(1, maxStackSize));
			}
		}

		// create item stack
		ItemStack itemStack = new ItemStack(item, quantity);
		randomlyModifyItem(itemStack, luck, registryAccess);
		return itemStack;
	}

	/**
	 * Applies various random modifications to an item including enchantments, attributes, and
	 * unbreaking.
	 *
	 * @param itemStack      item to modify
	 * @param luck           zero-indexed level of luck
	 * @param registryAccess registry accessor to load enchants from
	 */
	@Contract(mutates = "param1")
	public void randomlyModifyItem(ItemStack itemStack, int luck, @Nullable RegistryAccess registryAccess) {
		if (registryAccess == null)
			registryAccess = plugin.server().registryAccess();

		// make item unbreakable with a default chance of 5% (up to 100% at 10 luck)
		if (random.nextDouble() >= (UNBREAKABLE_BASE - (luck * UNBREAKABLE_DEC)))
			itemStack.getOrCreateTag().putBoolean("Unbreakable", true);

		// TODO: probably a better way to detect "normal" armor vs non-normal (tags?)
		if (random.nextInt(ARMOR_TRIM_ODDS) == 0 && itemStack.getItem() instanceof ArmorItem armor && armor.getEquipmentSlot().getType() == EquipmentSlot.Type.ARMOR && !itemStack.is(Items.ELYTRA) && !itemStack.is(Items.TURTLE_HELMET)) {
			ArmorTrim.setTrim(registryAccess, itemStack, new ArmorTrim(
				RandomUtil.randomElementFrom(plugin.registryHolders(Registries.TRIM_MATERIAL, registryAccess)),
				RandomUtil.randomElementFrom(plugin.registryHolders(Registries.TRIM_PATTERN, registryAccess))
			));
		}

		// determine enchantments to add
		int _enchantments = 0;
		for (int i = 0; i <= luck; i++) {
			_enchantments = Math.max(_enchantments, RandomUtil.weightedRandom(EnchantmentWeights.values(), EnchantmentWeights.TOTAL_WEIGHTS).getLevel());
		}
		final int enchantments = _enchantments;
		List<Enchantment> enchantmentList = BuiltInRegistries.ENCHANTMENT.stream()
			.filter(enchantment -> enchantment.canEnchant(itemStack))
			.collect(Collectors.toList());
		if (random.nextDouble() >= (.8d - (luck * .2d)))
			enchantmentList.removeIf(Enchantment::isCurse);

		// add enchantments
		List<Enchantment> addedEnchantments = new ArrayList<>(enchantments);
		ResourceLocation nullKey = new ResourceLocation("null", "null");
		while (addedEnchantments.size() < enchantments && !enchantmentList.isEmpty()) {
			Enchantment enchantment = enchantmentList.remove(0);

			// block conflicting vanilla enchantments (unless the die roll decides otherwise)
			boolean isVanilla = Objects.requireNonNullElse(BuiltInRegistries.ENCHANTMENT.getKey(enchantment), nullKey).getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE);
			if (addedEnchantments.stream().anyMatch(
				x -> !x.isCompatibleWith(enchantment)
				&& (
					// allow skipping if one of the enchants isn't vanilla
					// or if a 90% chance procs
					!isVanilla
						|| !Objects.requireNonNullElse(BuiltInRegistries.ENCHANTMENT.getKey(x), nullKey).getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)
						|| random.nextDouble() >= (.1d + (luck * .1d))
				)
			))
				continue;
			addedEnchantments.add(enchantment);

			// determine enchantment level
			int level = enchantment.getMinLevel();
			if (enchantment.getMaxLevel() > level) {
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
			EquipmentSlot[] equipmentSlots = itemStack.getItem() instanceof ArmorItem armorItem
				? new EquipmentSlot[]{armorItem.getEquipmentSlot()}
				: new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND};
			// add default attributes, which would otherwise be replaced by the custom ones
			for (EquipmentSlot type : equipmentSlots) {
				itemStack.getItem().getDefaultAttributeModifiers(type)
					.forEach((attribute, modifier) -> itemStack.addAttributeModifier(attribute, modifier, type));
			}
			// add custom attributes
			List<Attribute> attributeList = new ArrayList<>(ATTRIBUTES);
			Collections.shuffle(attributeList, random);
			for (int i = 0; i < attributeList.size() && i < attributes; i++) {
				Attribute attribute = attributeList.get(i);
				String name = "lootbox_" + Objects.requireNonNull(BuiltInRegistries.ATTRIBUTE.getKey(attribute)).getPath();
				// determine percent amount for the modifier
				double max = attribute == Attributes.MOVEMENT_SPEED ? 0.2d : 1d;
				double amount = -max; // just a fallback
				for (int j = 0; j <= luck; j++) {
					amount = Math.max(amount, random.nextDouble(-max, max + 0.001d));
				}
				if (Math.abs(amount) < 0.01d) continue;
				// create & add attribute
				for (int k = 0; k < equipmentSlots.length; k++) {
					AttributeModifier attributeModifier = new AttributeModifier(UUID.randomUUID(), name + "_" + k, amount, Operation.ADDITION);
					itemStack.addAttributeModifier(attribute, attributeModifier, equipmentSlots[k]);
				}
			}
		}
	}

	// lore getter
	private List<net.kyori.adventure.text.Component> getLore(ItemStack itemStack) {
		CompoundTag displayTag = itemStack.getTagElement(ItemStack.TAG_DISPLAY);
		if (displayTag == null)
			return new ArrayList<>();
		ListTag list = displayTag.getList(ItemStack.TAG_LORE, Tag.TAG_STRING);
		return list.stream()
			.map(tag -> net.minecraft.network.chat.Component.Serializer.fromJson(tag.getAsString()))
			.filter(Objects::nonNull)
			.map(plugin::toAdventure)
			.collect(Collectors.toList());
	}

	// lore setter
	private void setLore(ItemStack itemStack, List<net.kyori.adventure.text.Component> lore) {
		if (lore.isEmpty()) {
			CompoundTag displayTag = itemStack.getTagElement(ItemStack.TAG_DISPLAY);
			if (displayTag != null)
				displayTag.remove(ItemStack.TAG_LORE);
			return;
		}
		ListTag list = new ListTag();
		for (Component component : lore)
			list.add(StringTag.valueOf(net.minecraft.network.chat.Component.Serializer.toJson(plugin.adventure().toNative(component))));
		itemStack.getOrCreateTagElement(ItemStack.TAG_DISPLAY).put(ItemStack.TAG_LORE, list);
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			boolean success = false;
			List<ServerPlayer> players = playerSupplier.get();
			for (ServerPlayer player : players) {
				if (isLootboxOpen(player)) continue;

				Audience audience = plugin.adventure().audience(player);

				// init container
				SimpleContainer container = new SimpleContainer(27);
				for (int slot : CommandConstants.lootboxItemSlots(luck)) {
					ItemStack itemStack = createRandomItem(luck, player.getLevel().registryAccess());
					List<Component> lore = getLore(itemStack);
					lore.add(plugin.adventure().renderer().render(buildLootboxLore(plugin, request), audience));
					setLore(itemStack, lore);
					container.setItem(slot, itemStack);
				}

				// sound & open
				var title = plugin.adventure().toNative(buildLootboxTitle(plugin, request));

				OptionalInt val = player.openMenu(new SimpleMenuProvider((i, inventory, p) -> ChestMenu.threeRows(i, inventory, container), title));
				if (val.isEmpty()) continue;

				audience.playSound(Sounds.LOOTBOX_CHIME.get(luck), Sound.Emitter.self());
				OPEN_LOOTBOXES.put(player.getUUID(), (ChestMenu) player.containerMenu);
				TITLES.put(player.getUUID(), title);

				success = true;
			}
			return success
				? new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS)
				: new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Player has another lootbox open");
		}, plugin.getSyncExecutor()));
	}
}
