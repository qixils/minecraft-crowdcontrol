package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.common.CommandConstants.EnchantmentWeights;
import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.exceptions.ExceptionUtil;
import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.sound.SoundCategories;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
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
	private final String effectName = "lootbox";
	private final String displayName = "Open Lootbox";

	public LootboxCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
		allItems = new ArrayList<>(plugin.getRegistry().getAllOf(ItemType.class));
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		for (Player player : players) {
			Inventory lootbox = Inventory.builder()
					.of(ARCHETYPE)
					.property(new InventoryTitle(spongeSerializer.serialize(buildLootboxTitle(request))))
					.build(plugin);

			ItemType item = RandomUtil.randomElementFrom(allItems);
			ItemStack itemStack = ItemStack.of(item, 1 + random.nextInt(item.getMaxStackQuantity()));
			itemStack.offer(Keys.ITEM_LORE, Collections.singletonList(spongeSerializer.serialize(buildLootboxLore(request))));
			if (random.nextDouble() >= 0.9D) {
				itemStack.offer(Keys.UNBREAKABLE, true);
			}

			// enchantments
			int enchantments = RandomUtil.weightedRandom(EnchantmentWeights.values(), EnchantmentWeights.TOTAL_WEIGHTS).getLevel();
			if (enchantments > 0) {
				List<EnchantmentType> enchantmentList = new ArrayList<>(plugin.getRegistry().getAllOf(EnchantmentType.class));
				Collections.shuffle(enchantmentList, random);
				for (int i = 0; i < enchantmentList.size() && enchantments > 0; ++i) {
					EnchantmentType enchantment = enchantmentList.get(i);
					if (enchantment.canBeAppliedToStack(itemStack)) {
						enchantments--;
						itemStack.transform(Keys.ITEM_ENCHANTMENTS, enchants -> {
							enchants = ExceptionUtil.validateNotNullElseGet(enchants, ArrayList::new);
							int level = enchantment.getMinimumLevel()
									+ random.nextInt(enchantment.getMaximumLevel() - enchantment.getMinimumLevel() + 1);
							if (random.nextBoolean()) // bonus OP enchant chance
								level += random.nextInt(4);
							enchants.add(Enchantment.builder()
									.type(enchantment)
									.level(level)
									.build());
							return enchants;
						});
					}
				}
			}

			// API8: attributes
			// (attributes kinda exist in Sponge 7 but they are not available as a CatalogType, just
			//  as Keys, and even then I don't think I can add them to items)

			// the custom inventory does not implement anything sensible so enjoy this hack
			int i = 0;
			for (Inventory slot : lootbox.slots()) {
				if (i++ == 13) {
					slot.offer(itemStack);
					break;
				}
			}
			// sound & open
			player.playSound(
					SoundTypes.BLOCK_NOTE_CHIME,
					SoundCategories.PLAYER,
					player.getPosition(), // API8: should not use pos
					1d,
					1.2d
			);
			sync(() -> player.openInventory(lootbox));
		}
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
