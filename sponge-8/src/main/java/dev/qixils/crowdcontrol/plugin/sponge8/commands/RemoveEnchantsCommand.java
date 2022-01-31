package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.manipulator.mutable.item.EnchantmentData;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

@Getter
public final class RemoveEnchantsCommand extends ImmediateCommand {
	private final String effectName = "remove_enchants";
	private final String displayName = "Remove Enchants";

	public RemoveEnchantsCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(ResultType.RETRY)
				.message("Target was not holding an enchanted item");

		for (Player player : players) {
			if (tryRemoveEnchants(result, player.getItemInHand(HandTypes.MAIN_HAND)))
				continue;
			if (tryRemoveEnchants(result, player.getItemInHand(HandTypes.OFF_HAND)))
				continue;
			if (tryRemoveEnchants(result, player.getChestplate()))
				continue;
			if (tryRemoveEnchants(result, player.getLeggings()))
				continue;
			if (tryRemoveEnchants(result, player.getHelmet()))
				continue;
			tryRemoveEnchants(result, player.getBoots());
		}
		return result;
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	@Contract
	private boolean tryRemoveEnchants(Response.Builder result, Optional<ItemStack> optionalItem) {
		if (!optionalItem.isPresent())
			return false;
		ItemStack item = optionalItem.get();
		Optional<EnchantmentData> optionalEnchantmentData = item.get(EnchantmentData.class);
		if (!optionalEnchantmentData.isPresent())
			return false;
		EnchantmentData data = optionalEnchantmentData.get();
		ListValue<Enchantment> enchants = data.enchantments();
		if (enchants.isEmpty())
			return false;
		result.type(ResultType.SUCCESS).message("SUCCESS");
		item.offer(data.set(enchants.removeAll($ -> true)));
		return true;
	}
}
