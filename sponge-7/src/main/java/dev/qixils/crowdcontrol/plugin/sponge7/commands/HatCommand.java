package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

@Getter
public class HatCommand extends ImmediateCommand {
	private final String effectName = "hat";
	private final String displayName = "Put Item on Head";

	public HatCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder response = request.buildResponse()
				.type(ResultType.RETRY)
				.message("Held item(s) and hat are the same");

		for (Player player : players) {
			Optional<ItemStack> head = player.getHelmet();
			HandType usedHandType = null;
			Optional<ItemStack> hand = Optional.empty();
			for (HandType handType : plugin.getRegistry().getAllOf(HandType.class)) {
				hand = player.getItemInHand(handType);
				if (!isSimilar(hand, head)) {
					usedHandType = handType;
					break;
				}
			}
			if (usedHandType == null)
				continue;
			response.type(ResultType.SUCCESS).message("SUCCESS");
			player.setHelmet(hand.orElse(null));
			player.setItemInHand(usedHandType, head.orElse(null));
		}

		return response;
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	private boolean isSimilar(Optional<ItemStack> item1, Optional<ItemStack> item2) {
		if (!item1.isPresent() && !item2.isPresent())
			return true;
		if (!item1.isPresent())
			return false;
		if (!item2.isPresent())
			return false;
		ItemStack itemClone1 = item1.get().copy();
		itemClone1.setQuantity(1);
		ItemStack itemClone2 = item2.get().copy();
		itemClone2.setQuantity(1);
		return itemClone1.equalTo(itemClone2);
	}
}
