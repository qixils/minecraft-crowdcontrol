package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.common.util.sound.Sounds;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.RegionalCommandSync;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.*;

@Getter
public class LootboxCommand extends RegionalCommandSync {
	private static final List<Material> ITEMS = Registry.MATERIAL.stream().filter(mat -> mat.isItem() && !mat.isAir()).toList();
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
			Attribute.MAX_HEALTH,
			Attribute.KNOCKBACK_RESISTANCE,
			Attribute.MOVEMENT_SPEED,
			Attribute.ATTACK_DAMAGE,
			Attribute.ARMOR,
			Attribute.ARMOR_TOUGHNESS,
			Attribute.ATTACK_KNOCKBACK,
			Attribute.ATTACK_SPEED
	);
	private static final Map<UUID, Inventory> OPEN_LOOTBOXES = new HashMap<>();
	private final String effectName;
	private final int luck;

	public LootboxCommand(PaperCrowdControlPlugin plugin, int luck) {
		super(plugin);

		this.luck = luck;

		// set effect name to an ID like "lootbox_5" or just "lootbox" for luck level of 1
		StringBuilder effectName = new StringBuilder("lootbox");
		if (luck > 0)
			effectName.append('_').append(luck);
		this.effectName = effectName.toString();
	}

	private static boolean isLootboxOpen(@NotNull UUID uuid) {
		if (!OPEN_LOOTBOXES.containsKey(uuid)) return false;
		Inventory inv = OPEN_LOOTBOXES.get(uuid);
		if (inv.isEmpty() || inv.getViewers().isEmpty()) {
			OPEN_LOOTBOXES.remove(uuid);
			return false;
		}
		return true;
	}

	private static boolean isGoodItem(@Nullable Material item) {
		return item != null && GOOD_ITEMS.contains(item);
	}

	/**
	 * Creates a random item that is influenced by the supplied luck value.
	 * The item may contain enchantments and modifiers if applicable.
	 *
	 * @param luck           zero-indexed level of luck
	 * @param registryAccess access to a registry
	 * @return new random item
	 */
	public static ItemStack createRandomItem(int luck, @Nullable RegistryAccess registryAccess) {
		// determine the item used in the stack
		// "good" items have a higher likelihood of being picked with positive luck
		List<Material> items = new ArrayList<>(ITEMS);
		items.removeIf(item -> !PaperCrowdControlPlugin.isFeatureEnabled(item));
		Collections.shuffle(items, random);
		Material item = null;
		for (int i = 0; i <= luck * 5; i++) {
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
		randomlyModifyItem(itemStack, luck, registryAccess);
		return itemStack;
	}

	/**
	 * Applies various random modifications to an item including enchantments, attributes, and
	 * unbreaking.
	 *
	 * @param itemStack      item to modify
	 * @param luck           zero-indexed level of luck
	 * @param registryAccess access to a registry
	 */
	@Contract(mutates = "param1")
	public static void randomlyModifyItem(ItemStack itemStack, int luck, @Nullable RegistryAccess registryAccess) {
		// TODO: update usages with real registry accessors ...? seems to not exist yet...
		if (registryAccess == null)
			registryAccess = RegistryAccess.registryAccess();

		Material item = itemStack.getType();
		ItemMeta itemMeta = itemStack.getItemMeta();

		// make item unbreakable with a default chance of 5% (up to 100% at 10 luck)
		if (random.nextDouble() >= (UNBREAKABLE_BASE - (luck * UNBREAKABLE_DEC)))
			itemMeta.setUnbreakable(true);

		if (random.nextInt(ARMOR_TRIM_ODDS) == 0 && itemMeta instanceof ArmorMeta armorMeta) {
			armorMeta.setTrim(new ArmorTrim(
				RandomUtil.randomElementFrom(registryAccess.getRegistry(RegistryKey.TRIM_MATERIAL)),
				RandomUtil.randomElementFrom(registryAccess.getRegistry(RegistryKey.TRIM_PATTERN))
			));
		}

		// determine enchantments to add
		int enchantments = 0;
		for (int i = 0; i <= luck; i++) {
			enchantments = Math.max(enchantments, RandomUtil.weightedRandom(EnchantmentWeights.values(), EnchantmentWeights.TOTAL_WEIGHTS).getLevel());
		}
		List<Enchantment> enchantmentList = Registry.ENCHANTMENT.stream()
				.filter(enchantment -> enchantment.canEnchantItem(itemStack))
				.collect(Collectors.toList());
		if (random.nextDouble() >= (.8d - (luck * .2d)))
			enchantmentList.removeIf(Enchantment::isCursed);

		// add enchantments
		if (enchantments > 0 && !enchantmentList.isEmpty()) {
			Collections.shuffle(enchantmentList, random);
			List<Enchantment> addedEnchantments = new ArrayList<>(enchantments);
			for (int i = 0; i < enchantmentList.size() && addedEnchantments.size() < enchantments; ++i) {
				Enchantment enchantment = enchantmentList.get(i);
				// block conflicting enchantments (unless the die roll decides otherwise)
				if (addedEnchantments.stream().anyMatch(x -> x.conflictsWith(enchantment)) && random.nextDouble() >= (.1d + (luck * .1d)))
					continue;
				addedEnchantments.add(enchantment);
				// determine enchantment level
				int level = enchantment.getStartLevel();
				if (enchantment.getMaxLevel() > level) {
					for (int j = 0; j <= luck; j++) {
						level = Math.max(level, RandomUtil.nextInclusiveInt(enchantment.getStartLevel(), enchantment.getMaxLevel()));
					}
					if (random.nextDouble() >= (0.5D - (luck * .07D)))
						level += random.nextInt(4);
				}
				// add enchant
				itemMeta.addEnchant(enchantment, level, true);
			}
		}

		// determine attributes to add
		int attributes = 0;
		for (int i = 0; i <= luck; i++) {
			attributes = Math.max(attributes, RandomUtil.weightedRandom(AttributeWeights.values(), AttributeWeights.TOTAL_WEIGHTS).getLevel());
		}
		// add attributes
		if (attributes > 0
				&& item.getMaxDurability() <= 1 // TODO: add default attributes to items and remove this
		) {
			EquipmentSlot target = item.getEquipmentSlot();
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
				itemMeta.addAttributeModifier(attribute, createModifier(name, amount, target));
				if (target == EquipmentSlot.HAND)
					itemMeta.addAttributeModifier(attribute, createModifier(name + "_offhand", amount, EquipmentSlot.OFF_HAND));
			}
		}

		// finish up
		itemStack.setItemMeta(itemMeta);
	}

	@NotNull
	private static AttributeModifier createModifier(@NotNull String name, double amount, @NotNull EquipmentSlot slot) {
		return new AttributeModifier(
				UUID.randomUUID(), name, amount,
				AttributeModifier.Operation.MULTIPLY_SCALAR_1,
				slot
		);
	}

	@Override
	protected Response.@NotNull Builder buildFailure(@NotNull Request request) {
		return request.buildResponse()
			.type(Response.ResultType.RETRY)
			.message("Player already has a lootbox open");
	}

	@Override
	protected boolean executeRegionallySync(@NotNull Player player, @NotNull Request request) {
		if (isLootboxOpen(player.getUniqueId()))
			return false;

		Inventory lootbox = Bukkit.createInventory(null, 27, CommandConstants.buildLootboxTitle(plugin, request));
		for (int slot : lootboxItemSlots(luck)) {
			// create item
			ItemStack randomItem = createRandomItem(luck, null);
			ItemMeta itemMeta = randomItem.getItemMeta();
			itemMeta.lore(Collections.singletonList(GlobalTranslator.render(CommandConstants.buildLootboxLore(plugin, request), player.locale())));
			randomItem.setItemMeta(itemMeta);
			lootbox.setItem(slot, randomItem);
		}

		// display lootbox
		InventoryView view = player.openInventory(lootbox);
		if (view == null)
			return false;

		player.playSound(
			Sounds.LOOTBOX_CHIME.get(luck),
			Sound.Emitter.self()
		);

		OPEN_LOOTBOXES.put(player.getUniqueId(), lootbox);

		return true;
	}

	@RequiredArgsConstructor
	public static final class Manager implements Listener {
		private final PaperCrowdControlPlugin plugin;

		@EventHandler
		public void onCloseInventory(InventoryCloseEvent event) {
			HumanEntity player = event.getPlayer();
			UUID uuid = player.getUniqueId();

			if (event.getReason() != InventoryCloseEvent.Reason.PLAYER) {
				OPEN_LOOTBOXES.remove(uuid);
				return;
			}

			if (!isLootboxOpen(uuid))
				return;

			Inventory lootbox = OPEN_LOOTBOXES.get(uuid);
			player.getScheduler().run(plugin, $ -> player.openInventory(lootbox), null);
		}
	}
}
