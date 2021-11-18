package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

@Getter
public class DeleteItemCommand extends ImmediateCommand {
	public DeleteItemCommand(CrowdControlPlugin plugin) {
		super(plugin);
	}

	private final String effectName = "delete_item";
	private final String displayName = "Delete Held Item";

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull Request request) {
		Response.Builder result = request.buildResponse().type(Response.ResultType.FAILURE).message("No players were holding items");
		for (Player player : CrowdControlPlugin.getPlayers()) {
			PlayerInventory inv = player.getInventory();
			if (!inv.getItemInMainHand().getType().isEmpty()) {
				inv.setItemInMainHand(null);
				result.type(Response.ResultType.SUCCESS).message("SUCCESS");
			}
		}
		return result;
	}
}
