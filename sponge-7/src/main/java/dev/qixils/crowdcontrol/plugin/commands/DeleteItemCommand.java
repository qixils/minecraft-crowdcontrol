package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

@Getter
public class DeleteItemCommand extends ImmediateCommand {
	private final String effectName = "delete_item";
	private final String displayName = "Delete Held Item";

	public DeleteItemCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(Response.ResultType.RETRY)
				.message("No players were holding items");
		for (Player player : players) {
			for (HandType hand : plugin.getRegistry().getAllOf(HandType.class)) {
				Optional<ItemStack> optionalItem = player.getItemInHand(hand);
				if (!optionalItem.isPresent())
					continue;
				if (optionalItem.get().isEmpty())
					continue;
				result.type(Response.ResultType.SUCCESS).message("SUCCESS");
				player.setItemInHand(hand, null);
				break;
			}
		}

		return result;
	}
}
