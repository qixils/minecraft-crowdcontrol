package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class ClearInventoryCommand extends ImmediateCommand {
	private final String effectName = "clear_inventory";

	public ClearInventoryCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	@NotNull
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder resp = request.buildResponse()
				.type(ResultType.FAILURE)
				.message("All inventories are already empty or protected");
		for (Player player : players) {
			if (KeepInventoryCommand.isKeepingInventory(player))
				continue;
			PlayerInventory inv = player.getInventory();
			if (inv.isEmpty())
				continue;
			resp.type(ResultType.SUCCESS).message("SUCCESS");
			sync(inv::clear);
		}
		return resp;
	}
}
