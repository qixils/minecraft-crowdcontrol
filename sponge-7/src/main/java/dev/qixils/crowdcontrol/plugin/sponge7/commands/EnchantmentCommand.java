package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge7.utils.Slot;
import dev.qixils.crowdcontrol.plugin.sponge7.utils.SpongeTextUtil;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.*;
import java.util.Map.Entry;

@Getter
public class EnchantmentCommand extends ImmediateCommand {
	private final Component displayName;
	private final String effectName;
	private final EnchantmentType enchantmentType;
	private final int maxLevel;

	public EnchantmentCommand(SpongeCrowdControlPlugin plugin, EnchantmentType enchantment) {
		super(plugin);
		this.enchantmentType = enchantment;
		this.maxLevel = enchantment.getMaximumLevel();
		this.effectName = "enchant_" + SpongeTextUtil.csIdOf(enchantment);
		this.displayName = Component.translatable(
				"cc.effect.enchant.name",
				Component.translatable(
						enchantment.getTranslation().getId(),
						Component.text(enchantment.getMaximumLevel())
				)
		);
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
				if (curLevel == 255)
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

}
