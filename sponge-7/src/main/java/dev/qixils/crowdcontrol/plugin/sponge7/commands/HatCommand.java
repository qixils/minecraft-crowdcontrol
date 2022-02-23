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

import static dev.qixils.crowdcontrol.plugin.sponge7.utils.ItemUtil.isSimilar;

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
			for (HandType handType : plugin.getRegistry().getAllOf(HandType.class)) {
				Optional<ItemStack> hand = player.getItemInHand(handType);
				if (isSimilar(hand, head))
					continue;
				response.type(ResultType.SUCCESS).message("SUCCESS");
				player.setHelmet(hand.orElse(null));
				player.setItemInHand(handType, head.orElse(null));
				break;
			}
		}

		return response;
	}
}
