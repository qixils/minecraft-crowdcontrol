package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.Command;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

@Getter
public class DeleteItemCommand extends Command {
	public DeleteItemCommand(CrowdControlPlugin plugin) {
		super(plugin);
	}

	private final String effectName = "delete-item";
	private final String displayName = "Delete Held Item";

	@Override
	public Response.@NotNull Result execute(@NotNull Request request) {
		Response.Result result = new Response.Result(Response.ResultType.FAILURE, "No players were holding items");
		for (Player player : CrowdControlPlugin.getPlayers()) {
			PlayerInventory inv = player.getInventory();
			if (!inv.getItemInMainHand().getType().isEmpty()) {
				inv.setItemInMainHand(null);
				result = Response.Result.SUCCESS;
			}
		}
		return result;
	}
}
