package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.registry.RegistryTypes;

import java.util.List;

@Getter
public class DeleteItemCommand extends ImmediateCommand {
	private final String effectName = "delete_item";
	private final String displayName = "Delete Held Item";

	public DeleteItemCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(Response.ResultType.RETRY)
				.message("No players were holding items");
		for (Player player : players) {
			for (HandType hand : plugin.registryIterable(RegistryTypes.HAND_TYPE)) {
				if (player.itemInHand(hand).isEmpty())
					continue;
				result.type(Response.ResultType.SUCCESS).message("SUCCESS");
				player.setItemInHand(hand, null);
				break;
			}
		}

		return result;
	}
}
