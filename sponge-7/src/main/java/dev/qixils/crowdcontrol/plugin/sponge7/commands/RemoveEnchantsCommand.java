package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

@Getter
public final class RemoveEnchantsCommand extends ImmediateCommand {
	private final String effectName = "remove_enchants";

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
		Optional<List<Enchantment>> optionalEnchantments = optionalItem
				.flatMap(item -> item.get(Keys.ITEM_ENCHANTMENTS));
		if (!optionalEnchantments.isPresent() || optionalEnchantments.get().isEmpty())
			return false;
		result.type(ResultType.SUCCESS).message("SUCCESS");
		optionalItem.get().remove(Keys.ITEM_ENCHANTMENTS);
		return true;
	}
}
