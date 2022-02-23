package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.registry.RegistryTypes;

import java.util.List;

@Getter
public class HatCommand extends ImmediateCommand {
	private final String effectName = "hat";
	private final String displayName = "Put Item on Head";

	public HatCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder response = request.buildResponse()
				.type(ResultType.RETRY)
				.message("Held item(s) and hat are the same");

		for (ServerPlayer player : players) {
			ItemStack head = player.head();
			for (HandType handType : plugin.registryIterable(RegistryTypes.HAND_TYPE)) {
				ItemStack hand = player.itemInHand(handType);
				if (isSimilar(hand, head))
					continue;
				response.type(ResultType.SUCCESS).message("SUCCESS");
				sync(() -> {
					player.setHead(hand);
					player.setItemInHand(handType, head);
				});
				break;
			}
		}

		return response;
	}

	private boolean isSimilar(ItemStack item1, ItemStack item2) {
		if (item1.isEmpty() && item2.isEmpty())
			return true;
		if (item1.isEmpty())
			return false;
		if (item2.isEmpty())
			return false;
		ItemStack itemClone1 = item1.copy();
		itemClone1.setQuantity(1);
		ItemStack itemClone2 = item2.copy();
		itemClone2.setQuantity(1);
		return itemClone1.equalTo(itemClone2);
	}
}
