package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.utils.Sponge7TextUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.manipulator.mutable.item.EnchantmentData;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Iterator;
import java.util.List;
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
		this.effectName = "enchant_" + Sponge7TextUtil.valueOf(enchantmentType);
		this.displayName = "Apply " + enchantmentType.getTranslation().get(); // TODO: enchant level
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder response = request.buildResponse()
				.type(ResultType.FAILURE)
				.message("");
		for (Player player : players) {
			Optional<ItemStack> optionalStack = player.getItemInHand(HandTypes.MAIN_HAND);
			if (!optionalStack.isPresent())
				optionalStack = player.getItemInHand(HandTypes.OFF_HAND);
			if (!optionalStack.isPresent())
				continue;

			ItemStack item = optionalStack.get();

			// Here is where I'd check if the item stack supports this enchantment
			// but this is not available in API 7 apparently

			if (!item.getOrCreate(EnchantmentData.class).isPresent())
				continue;

			Optional<EnchantmentData> optionalEnchantmentData = item.getOrCreate(EnchantmentData.class);
			if (!optionalEnchantmentData.isPresent())
				continue;
			response.type(ResultType.SUCCESS).message("SUCCESS");
			Enchantment toAdd = Enchantment.of(enchantmentType, maxLevel);
			EnchantmentData enchantmentData = optionalEnchantmentData.get();
			ListValue<Enchantment> enchantments = enchantmentData.enchantments();
			Iterator<Enchantment> iterator = enchantments.iterator();
			while (iterator.hasNext()) {
				Enchantment enchantment = iterator.next();
				if (!enchantment.getType().equals(enchantmentType))
					continue;
				if (enchantment.getLevel() == maxLevel)
					toAdd = Enchantment.of(enchantmentType, maxLevel + 1);
				iterator.remove();
			}
			enchantments.add(toAdd);
			// I think these are necessary
			enchantmentData.set(enchantments);
			item.offer(enchantmentData);
		}
		return response;
	}
}
