package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.RegionalCommandSync;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

@Getter
public class DeleteItemCommand extends RegionalCommandSync {
	private final String effectName = "delete_item";

	public DeleteItemCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	protected Response.@NotNull Builder buildFailure(@NotNull Request request) {
		return request.buildResponse()
			.type(Response.ResultType.RETRY)
			.message("No players were holding items");
	}

	@Override
	protected boolean executeRegionallySync(@NotNull Player player, @NotNull Request request) {
		PlayerInventory inv = player.getInventory();
		if (!inv.getItemInMainHand().getType().isEmpty()) {
			inv.setItemInMainHand(null);
			return true;
		}
		if (!inv.getItemInOffHand().getType().isEmpty()) {
			inv.setItemInOffHand(null);
			return true;
		}
		return false;
	}
}
