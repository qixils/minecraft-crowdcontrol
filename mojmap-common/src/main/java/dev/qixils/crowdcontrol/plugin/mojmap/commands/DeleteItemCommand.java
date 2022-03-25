package dev.qixils.crowdcontrol.plugin.mojmap.commands;

import dev.qixils.crowdcontrol.plugin.mojmap.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.mojmap.MojmapPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class DeleteItemCommand extends ImmediateCommand {
	private final String effectName = "delete_item";
	private final String displayName = "Delete Held Item";

	public DeleteItemCommand(MojmapPlugin plugin) {
		super(plugin);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(Response.ResultType.RETRY)
				.message("No players were holding items");
		for (ServerPlayer player : players) {
			for (InteractionHand hand : InteractionHand.values()) {
				if (player.getItemInHand(hand).isEmpty())
					continue;
				result.type(Response.ResultType.SUCCESS).message("SUCCESS");
				sync(() -> player.setItemInHand(hand, ItemStack.EMPTY));
				break;
			}
		}

		return result;
	}
}
