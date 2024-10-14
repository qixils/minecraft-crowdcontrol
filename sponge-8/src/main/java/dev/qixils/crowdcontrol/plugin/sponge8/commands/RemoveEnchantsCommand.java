package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
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
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(ResultType.RETRY)
				.message("Target was not holding an enchanted item");

		for (ServerPlayer player : players) {
			if (tryRemoveEnchants(result, player.itemInHand(HandTypes.MAIN_HAND)))
				continue;
			if (tryRemoveEnchants(result, player.itemInHand(HandTypes.OFF_HAND)))
				continue;
			if (tryRemoveEnchants(result, player.chest()))
				continue;
			if (tryRemoveEnchants(result, player.legs()))
				continue;
			if (tryRemoveEnchants(result, player.head()))
				continue;
			tryRemoveEnchants(result, player.feet());
		}
		return result;
	}

	@Contract
	private boolean tryRemoveEnchants(Response.Builder result, ItemStack item) {
		if (item.isEmpty())
			return false;
		Optional<List<Enchantment>> optionalEnchantments = item.get(Keys.APPLIED_ENCHANTMENTS);
		if (!optionalEnchantments.isPresent())
			return false;
		List<Enchantment> enchants = optionalEnchantments.get();
		if (enchants.isEmpty())
			return false;
		result.type(ResultType.SUCCESS).message("SUCCESS");
		item.remove(Keys.APPLIED_ENCHANTMENTS);
		return true;
	}
}
