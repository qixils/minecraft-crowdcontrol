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

import static dev.qixils.crowdcontrol.plugin.sponge8.utils.ItemUtil.isSimilar;

@Getter
public class HatCommand extends ImmediateCommand {
	private final String effectName = "hat";

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
}
