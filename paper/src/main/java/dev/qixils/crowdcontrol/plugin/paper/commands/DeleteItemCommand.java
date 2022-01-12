package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class DeleteItemCommand extends ImmediateCommand {
	private final String effectName = "delete_item";
	private final String displayName = "Delete Held Item";

	public DeleteItemCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(Response.ResultType.RETRY)
				.message("No players were holding items");
		for (Player player : players) {
			PlayerInventory inv = player.getInventory();
			if (!inv.getItemInMainHand().getType().isEmpty()) {
				inv.setItemInMainHand(null);
				result.type(Response.ResultType.SUCCESS).message("SUCCESS");
			} else if (!inv.getItemInOffHand().getType().isEmpty()) {
				inv.setItemInOffHand(null);
				result.type(Response.ResultType.SUCCESS).message("SUCCESS");
			}
		}
		return result;
	}
}
