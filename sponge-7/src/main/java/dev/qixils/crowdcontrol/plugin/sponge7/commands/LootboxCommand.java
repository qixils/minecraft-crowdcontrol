package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import com.flowpowered.math.vector.Vector3d;
import dev.qixils.crowdcontrol.common.ExecuteUsing;
import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.common.util.sound.Sounds;
import dev.qixils.crowdcontrol.exceptions.ExceptionUtil;
import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.kyori.adventure.translation.GlobalTranslator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.property.item.UseLimitProperty;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.inventory.*;
import org.spongepowered.api.item.inventory.property.InventoryCapacity;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

import java.util.*;
import java.util.stream.Collectors;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.*;

@ExecuteUsing(ExecuteUsing.Type.SYNC_GLOBAL)
@Getter
public class LootboxCommand extends ImmediateCommand {
	private static final InventoryArchetype ARCHETYPE = InventoryArchetype.builder()
			.from(InventoryArchetypes.MENU_GRID)
			.title(Text.of("Lootbox"))
			.property(InventoryCapacity.of(27))
			.build("crowdcontrol:lootbox", "Lootbox");
	private static final Map<UUID, Inventory> OPEN_LOOTBOXES = new HashMap<>();
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
		allItems = plugin.getRegistry().getAllOf(ItemType.class).stream().filter(it -> it != ItemTypes.AIR).collect(Collectors.toList());
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

	private static boolean isLootboxOpen(@NotNull UUID uuid) {
		if (!OPEN_LOOTBOXES.containsKey(uuid)) return false;
		Inventory inventory = OPEN_LOOTBOXES.get(uuid);
		Container container = CONTAINERS.get(uuid);
		if (inventory.size() == 0 || !container.hasViewers()) {
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
	public @NotNull ItemStack createRandomItem(int luck) {
		// determine the item used in the stack
		// "good" items have a higher likelihood of being picked with positive luck
		List<ItemType> items = new ArrayList<>(allItems);
		Collections.shuffle(items, random);
		ItemType item = null;
		// multiplier is slightly lower on sponge than paper due to smaller item count
		// (due to a ton of items just. having the same item ID.)
		for (int i = 0; i <= luck * 3; i++) {
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
		randomlyModifyItem(itemStack, luck);
		return itemStack;
	}

	/**
	 * Applies various random modifications to an item including enchantments and unbreaking.
	 *
	 * @param itemStack item to modify
	 * @param luck      zero-indexed level of luck
	 */
	@Contract(mutates = "param1")
	public void randomlyModifyItem(ItemStack itemStack, int luck) {
		// make item unbreakable with a default chance of 5% (up to 100% at 10 luck)
		if (random.nextDouble() >= (UNBREAKABLE_BASE - (luck * UNBREAKABLE_DEC)))
			itemStack.offer(Keys.UNBREAKABLE, true);

		// determine enchantments to add
		int _enchantments = 0;
		for (int i = 0; i <= luck; i++) {
			_enchantments = Math.max(_enchantments, RandomUtil.weightedRandom(EnchantmentWeights.values(), EnchantmentWeights.TOTAL_WEIGHTS).getLevel());
		}
		final int enchantments = _enchantments;
		List<EnchantmentType> enchantmentList = plugin.getRegistry().getAllOf(EnchantmentType.class).stream()
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
			int level = enchantment.getMinimumLevel();
			if (enchantment.getMaximumLevel() > level) {
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
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		boolean success = false;
		for (Player player : players) {
			if (isLootboxOpen(player.getUniqueId())) continue;

			Inventory lootbox = Inventory.builder()
					.of(ARCHETYPE)
					.property(new InventoryTitle(spongeSerializer.serialize(GlobalTranslator.render(buildLootboxTitle(plugin, request), player.getLocale()))))
					.build(plugin);

			// add items
			for (int slot : CommandConstants.lootboxItemSlots(luck)) {
				// create item
				ItemStack itemStack = createRandomItem(luck);
				itemStack.offer(
						Keys.ITEM_LORE,
						Collections.singletonList(spongeSerializer.serialize(GlobalTranslator.render(buildLootboxLore(plugin, request), player.getLocale())))
				);

				// the custom inventory does not implement anything sensible so enjoy this hack
				int i = 0;
				for (Inventory slotInv : lootbox.slots()) {
					if (i++ == slot) {
						slotInv.offer(itemStack);
						break;
					}
				}
			}

			// sound & open
			Vector3d pos = player.getPosition();
			Container container = player.openInventory(lootbox).orElse(null);
			if (container == null) continue;

			plugin.asAudience(player).playSound(Sounds.LOOTBOX_CHIME.get(luck), pos.getX(), pos.getY(), pos.getZ());
			OPEN_LOOTBOXES.put(player.getUniqueId(), lootbox);
			CONTAINERS.put(player.getUniqueId(), container);

			success = true;
		}
		return success
			? request.buildResponse().type(Response.ResultType.SUCCESS)
			: request.buildResponse().type(Response.ResultType.RETRY).message("Player has another lootbox open");
	}

	public static final class Manager {
		@Listener
		public void onInventoryClose(InteractInventoryEvent.Close event) {
			Set<Player> viewers = event.getTargetInventory().getViewers();
			if (viewers.isEmpty()) return;
			Player player = viewers.iterator().next();
			UUID uuid = player.getUniqueId();

			if (event.getCause().containsType(PluginContainer.class)) {
				OPEN_LOOTBOXES.remove(uuid);
				return;
			}

			if (!isLootboxOpen(uuid))
				return;

			Inventory lootbox = OPEN_LOOTBOXES.get(uuid);
			player.openInventory(lootbox).ifPresent(container -> CONTAINERS.put(uuid, container));
		}
	}
}
