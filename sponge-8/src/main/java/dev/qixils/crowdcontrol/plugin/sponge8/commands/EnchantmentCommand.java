package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge8.utils.Sponge7TextUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

@Getter
public class EnchantmentCommand extends ImmediateCommand {
	private final String displayName;
	private final String effectName;
	private final EnchantmentType enchantmentType;
	private final int maxLevel;

	public EnchantmentCommand(SpongeCrowdControlPlugin plugin, EnchantmentType enchantmentType) {
		super(plugin);
		this.enchantmentType = enchantmentType;
		this.maxLevel = enchantmentType.getMaximumLevel();
		this.effectName = "enchant_" + Sponge7TextUtil.csIdOf(enchantmentType);
		this.displayName = "Apply " + enchantmentType.getTranslation().get();
	}

	private int getCurrentLevel(ItemStack item) {
		return item.get(Keys.ITEM_ENCHANTMENTS)
				.flatMap(enchs -> enchs.stream().filter(ench -> ench.getType().equals(enchantmentType)).findFirst())
				.map(Enchantment::getLevel).orElse(0);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder response = request.buildResponse()
				.type(ResultType.RETRY)
				.message("No items could be enchanted");
		for (Player player : players) {
			// get the equipped item that supports this enchantment and has the lowest level of it
			Map<Slot, Integer> levelMap = new HashMap<>(Slot.values().length);
			for (Slot slot : Slot.values()) {
				Optional<ItemStack> optionalStack = slot.getItem(player);
				if (!optionalStack.isPresent())
					continue;
				ItemStack item = optionalStack.get();
				if (item.isEmpty())
					continue;
				if (!enchantmentType.canBeAppliedToStack(item))
					continue;
				int curLevel = getCurrentLevel(item);
				if (enchantmentType.getMaximumLevel() == enchantmentType.getMinimumLevel() && curLevel == enchantmentType.getMaximumLevel())
					continue;
				levelMap.put(slot, curLevel);
			}
			Slot slot = levelMap.entrySet().stream()
					.min(Comparator.comparingInt(Entry::getValue))
					.map(Entry::getKey).orElse(null);
			if (slot == null)
				continue;
			Optional<ItemStack> optionalStack = slot.getItem(player);
			if (!optionalStack.isPresent())
				continue;

			// misc instantiation
			ItemStack item = optionalStack.get();
			List<Enchantment> enchantments = new ArrayList<>(item.get(Keys.ITEM_ENCHANTMENTS).orElseGet(Collections::emptyList));
			Iterator<Enchantment> iterator = enchantments.iterator();

			// determine enchantment level
			Enchantment toAdd = Enchantment.of(enchantmentType, maxLevel);
			while (iterator.hasNext()) {
				Enchantment enchantment = iterator.next();
				if (!enchantment.getType().equals(enchantmentType))
					continue;
				int curLevel = enchantment.getLevel();
				if (curLevel >= maxLevel)
					toAdd = Enchantment.of(enchantmentType, curLevel + 1);
				iterator.remove();
			}

			// add enchant
			enchantments.add(toAdd);
			item.offer(Keys.ITEM_ENCHANTMENTS, enchantments);
			response.type(ResultType.SUCCESS).message("SUCCESS");
		}
		return response;
	}

	private enum Slot {
		MAIN_HAND {
			@Override
			public Optional<ItemStack> getItem(Player player) {
				return player.getItemInHand(HandTypes.MAIN_HAND);
			}

			@Override
			public void setItem(Player player, ItemStack item) {
				player.setItemInHand(HandTypes.MAIN_HAND, item);
			}
		},
		OFF_HAND {
			@Override
			public Optional<ItemStack> getItem(Player player) {
				return player.getItemInHand(HandTypes.OFF_HAND);
			}

			@Override
			public void setItem(Player player, ItemStack item) {
				player.setItemInHand(HandTypes.OFF_HAND, item);
			}
		},
		HELMET {
			@Override
			public Optional<ItemStack> getItem(Player player) {
				return player.getHelmet();
			}

			@Override
			public void setItem(Player player, ItemStack item) {
				player.setHelmet(item);
			}
		},
		CHESTPLATE {
			@Override
			public Optional<ItemStack> getItem(Player player) {
				return player.getChestplate();
			}

			@Override
			public void setItem(Player player, ItemStack item) {
				player.setChestplate(item);
			}
		},
		LEGGINGS {
			@Override
			public Optional<ItemStack> getItem(Player player) {
				return player.getLeggings();
			}

			@Override
			public void setItem(Player player, ItemStack item) {
				player.setLeggings(item);
			}
		},
		BOOTS {
			@Override
			public Optional<ItemStack> getItem(Player player) {
				return player.getBoots();
			}

			@Override
			public void setItem(Player player, ItemStack item) {
				player.setBoots(item);
			}
		};

		public abstract Optional<ItemStack> getItem(Player player);

		public abstract void setItem(Player player, ItemStack item);
	}
}
