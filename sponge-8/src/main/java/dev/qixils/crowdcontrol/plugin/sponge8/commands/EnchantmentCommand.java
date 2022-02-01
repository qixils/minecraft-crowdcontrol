package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.registry.RegistryTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Getter
public class EnchantmentCommand extends ImmediateCommand {
	private final String displayName;
	private final String effectName;
	private final EnchantmentType enchantmentType;
	private final int maxLevel;

	public EnchantmentCommand(SpongeCrowdControlPlugin plugin, EnchantmentType enchantmentType) {
		super(plugin);
		this.enchantmentType = enchantmentType;
		this.maxLevel = enchantmentType.maximumLevel();
		this.effectName = "enchant_" + enchantmentType.key(RegistryTypes.ENCHANTMENT_TYPE).value();
		this.displayName = "Apply " + plugin.getTextUtil().asPlain(enchantmentType);
	}

	private int getCurrentLevel(ItemStack item) {
		return item.get(Keys.APPLIED_ENCHANTMENTS)
				.flatMap(enchantments -> enchantments.stream()
						.filter(enchantment -> enchantment.type().equals(enchantmentType))
						.findFirst()
				).map(Enchantment::level).orElse(0);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder response = request.buildResponse()
				.type(ResultType.RETRY)
				.message("No items could be enchanted");
		for (ServerPlayer player : players) {
			// get the equipped item that supports this enchantment and has the lowest level of it
			Map<Slot, Integer> levelMap = new HashMap<>(Slot.values().length);
			for (Slot slot : Slot.values()) {
				ItemStack item = slot.getItem(player);
				if (item.isEmpty())
					continue;
				if (!enchantmentType.canBeAppliedToStack(item))
					continue;
				int curLevel = getCurrentLevel(item);
				if (enchantmentType.maximumLevel() == enchantmentType.minimumLevel() && curLevel == enchantmentType.maximumLevel())
					continue;
				levelMap.put(slot, curLevel);
			}
			Slot slot = levelMap.entrySet().stream()
					.min(Comparator.comparingInt(Entry::getValue))
					.map(Entry::getKey).orElse(null);
			if (slot == null)
				continue;
			ItemStack item = slot.getItem(player);

			// misc instantiation
			List<Enchantment> enchantments = new ArrayList<>(item.get(Keys.APPLIED_ENCHANTMENTS).orElseGet(Collections::emptyList));
			Iterator<Enchantment> iterator = enchantments.iterator();

			// determine enchantment level
			Enchantment toAdd = Enchantment.of(enchantmentType, maxLevel);
			while (iterator.hasNext()) {
				Enchantment enchantment = iterator.next();
				if (!enchantment.type().equals(enchantmentType))
					continue;
				int curLevel = enchantment.level();
				if (curLevel >= maxLevel)
					toAdd = Enchantment.of(enchantmentType, curLevel + 1);
				iterator.remove();
			}

			// add enchant
			enchantments.add(toAdd);
			item.offer(Keys.APPLIED_ENCHANTMENTS, enchantments);
			response.type(ResultType.SUCCESS).message("SUCCESS");
		}
		return response;
	}

	private enum Slot {
		MAIN_HAND {
			@Override
			public ItemStack getItem(Player player) {
				return player.itemInHand(HandTypes.MAIN_HAND);
			}
		},
		OFF_HAND {
			@Override
			public ItemStack getItem(Player player) {
				return player.itemInHand(HandTypes.OFF_HAND);
			}
		},
		HELMET {
			@Override
			public ItemStack getItem(Player player) {
				return player.head();
			}
		},
		CHESTPLATE {
			@Override
			public ItemStack getItem(Player player) {
				return player.chest();
			}
		},
		LEGGINGS {
			@Override
			public ItemStack getItem(Player player) {
				return player.legs();
			}
		},
		BOOTS {
			@Override
			public ItemStack getItem(Player player) {
				return player.feet();
			}
		};

		public abstract ItemStack getItem(Player player);
	}
}
